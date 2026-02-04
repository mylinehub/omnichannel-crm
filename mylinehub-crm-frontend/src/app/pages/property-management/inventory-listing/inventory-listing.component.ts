import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, takeWhile } from 'rxjs/operators';
import { LocalDataSource } from 'ng2-smart-table';
import { NbThemeService } from '@nebular/theme';
import { CustomInputTableComponent } from '../../employee/all-employees/custom-input-table/custom-input-table.component';
import { PropertyInventoryService } from '../service/property-inventory.service';
import { ConstantsService } from '../../../service/constants/constants.service';

@Component({
  selector: 'ngx-inventory-listing',
  templateUrl: './inventory-listing.component.html',
  styleUrls: ['./inventory-listing.component.scss']
})
export class InventoryListingComponent implements OnInit, OnDestroy {

  private static readonly DEEP_LOGS = true;

  // mimic customers behavior
  private static readonly CONTEXT_WAIT_MAX_RETRIES = 20;     // 20 * 300ms = 6s
  private static readonly CONTEXT_WAIT_INTERVAL_MS = 300;

  @ViewChild('autoPageNumber') pageNumberInput: any;
  @ViewChild('searchName') searchTextInput: any;

  private destroy$: Subject<void> = new Subject<void>();
  private alive = true;

  organization = '';
  searchString: string = '';
  currentPageNumber: number = 1;

  // Backend paging is 0-based. UI is 1-based.
  currentPageSize: number = 10;

  tableHeading = 'Property Inventory';
  totalPages: number = 0;

  // ✅ NEW: ONE boolean used by both table + excel
  inventoryAvailable: boolean = true;

  // Section 4
  showDetail: boolean = false;
  keys: any[] = [];
  values: any[] = [];

  // Chart
  single: any[] = [
    { name: 'Total', value: 0 }
  ];

  view: any = [600, 200];
  gradient: boolean = false;
  colorScheme: any;

  // Table source
  source: LocalDataSource = new LocalDataSource();

  // debounce timer
  private setSearchTextChangeId: any = null;

  // context wait timer (like customers delay)
  private contextWaitTimer: any = null;
  private contextWaitRetryCount = 0;
  private initialLoaded = false;

  // download
  fromListedDateLocal: Date | null = null;
  downloadError: string = '';

  // last loaded page cache (optional)
  private lastPageRows: any[] = [];

  settings = {
    actions: { add: false, edit: false, delete: false },
    hideSubHeader: true,
    pager: { display: false },

    columns: {
      customerId: { title: 'Customer ID', type: 'number' },

      customerName: {
        title: 'Customer Name',
        type: 'custom',
        valuePrepareFunction: (cell, row) => cell,
        renderComponent: CustomInputTableComponent,
      },

      customerPhoneNumber: {
        title: 'Phone Number',
        type: 'custom',
        valuePrepareFunction: (cell, row) => cell,
        renderComponent: CustomInputTableComponent,
      },

      customerCity: { title: 'City', type: 'string' },
      propertyType: { title: 'Property Type', type: 'string' },
      purpose: { title: 'Purpose', type: 'string' },
      rent: { title: 'Rent', type: 'string' },
      rentValue: { title: 'Rent Value', type: 'number' },
      bhk: { title: 'BHK', type: 'number' },
      furnishedType: { title: 'Furnished', type: 'string' },
      sqFt: { title: 'SqFt', type: 'number' },
      city: { title: 'Inv City', type: 'string' },
      area: { title: 'Area', type: 'string' },
      callStatus: { title: 'Call Status', type: 'string' },
      pid: { title: 'PID', type: 'string' },
    },
  };

  constructor(
    private inventoryService: PropertyInventoryService,
    private themeService: NbThemeService,
  ) {
    // keep this, but also re-check later (localStorage can be late in some flows)
    const org = localStorage.getItem('organization');
    if (org != null) this.organization = org;

    this.themeService.getJsTheme()
      .pipe(takeWhile(() => this.alive))
      .subscribe(theme => {
        const colors: any = theme.variables;
        this.colorScheme = {
          domain: [
            colors.primaryLight,
            colors.infoLight,
            colors.successLight,
            colors.warningLight,
            colors.dangerLight
          ],
        };
      });
  }

  ngOnInit(): void {
    if (InventoryListingComponent.DEEP_LOGS) {
      console.log('[INV] ngOnInit start org(localStorage)=', this.organization, 'user=', ConstantsService?.user);
    }

    // ✅ DO NOT call API immediately
    // ✅ Wait until context is ready (like customers)
    this.waitForContextAndLoad();
  }

  ngOnDestroy(): void {
    this.alive = false;

    if (this.setSearchTextChangeId != null) {
      clearTimeout(this.setSearchTextChangeId);
      this.setSearchTextChangeId = null;
    }

    if (this.contextWaitTimer != null) {
      clearTimeout(this.contextWaitTimer);
      this.contextWaitTimer = null;
    }

    this.destroy$.next();
    this.destroy$.complete();
  }

  // ✅ NEW: when radio changes -> full reload (page no, totals, table, etc)
  onAvailableChange(val: any): void {
    // ensure boolean
    this.inventoryAvailable = (val === true || val === 'true');

    if (InventoryListingComponent.DEEP_LOGS) {
      console.log('[INV] onAvailableChange inventoryAvailable=', this.inventoryAvailable);
    }

    if (!this.initialLoaded) {
      if (InventoryListingComponent.DEEP_LOGS) {
        console.log('[INV] skip availableChange because initialLoaded=false (context not ready yet)');
      }
      return;
    }

    // full reload behaviour
    this.currentPageNumber = 1;
    this.totalPages = 0;
    this.showDetail = false;
    this.keys = [];
    this.values = [];
    this.lastPageRows = [];
    this.source.load([]);

    // cancel any pending debounce
    if (this.setSearchTextChangeId != null) {
      clearTimeout(this.setSearchTextChangeId);
      this.setSearchTextChangeId = null;
    }

    this.loadPageFromServer('availableToggle');
  }

  // ----------------------------
  // Context wait (like Customers)
  // ----------------------------
  private waitForContextAndLoad(): void {
    // refresh org each time
    const org = localStorage.getItem('organization');
    if (org != null) this.organization = org;

    const orgReady = !!(this.organization && this.organization.trim().length > 0);

    // If you want EXACT customers-style gating:
    const userReady = (ConstantsService?.user && ConstantsService.user.firstName !== undefined);

    if (InventoryListingComponent.DEEP_LOGS) {
      console.log('[INV] waitForContextAndLoad retry=', this.contextWaitRetryCount, {
        org: this.organization,
        orgReady,
        userReady,
      });
    }

    if (orgReady && userReady) {
      if (!this.initialLoaded) {
        this.initialLoaded = true;
        if (InventoryListingComponent.DEEP_LOGS) {
          console.log('[INV] context ready -> initial load');
        }
        this.loadPageFromServer('initialContextReady');
      }
      return;
    }

    // keep waiting (bounded)
    if (this.contextWaitRetryCount >= InventoryListingComponent.CONTEXT_WAIT_MAX_RETRIES) {
      if (InventoryListingComponent.DEEP_LOGS) {
        console.log('[INV] context NOT ready after retries. Will not auto-load.', {
          org: this.organization,
          user: ConstantsService?.user,
        });
      }
      return;
    }

    this.contextWaitRetryCount++;
    this.contextWaitTimer = setTimeout(
      () => this.waitForContextAndLoad(),
      InventoryListingComponent.CONTEXT_WAIT_INTERVAL_MS
    );
  }

  // ----------------------------
  // Search & page change handlers
  // ----------------------------
  onSearchTextChange(): void {
    if (InventoryListingComponent.DEEP_LOGS) {
      console.log('[INV] onSearchTextChange searchString=', this.searchString);
    }

    if (!this.initialLoaded) {
      if (InventoryListingComponent.DEEP_LOGS) {
        console.log('[INV] skip searchChange because initialLoaded=false (context not ready yet)');
      }
      return;
    }

    this.currentPageNumber = 1;
    this.debounceLoad('searchChange', 700);
  }

  onPageNumberChange(): void {
    if (InventoryListingComponent.DEEP_LOGS) {
      console.log('[INV] onPageNumberChange currentPageNumber=', this.currentPageNumber);
    }

    if (!this.initialLoaded) {
      if (InventoryListingComponent.DEEP_LOGS) {
        console.log('[INV] skip pageChange because initialLoaded=false (context not ready yet)');
      }
      return;
    }

    this.debounceLoad('pageChange', 400);
  }

  verifyAlphaNumericConstraint(event: any): void {
    const k = event.keyCode;

    // only digits + backspace
    if (!((k >= 48 && k <= 57) || k === 8)) {
      event.preventDefault();
      return;
    }

    if (!this.initialLoaded) {
      if (InventoryListingComponent.DEEP_LOGS) {
        console.log('[INV] skip keydown load because initialLoaded=false (context not ready yet)');
      }
      return;
    }

    this.debounceLoad('keydown', 400);
  }

  private debounceLoad(reason: string, delayMs: number): void {
    if (this.setSearchTextChangeId != null) {
      clearTimeout(this.setSearchTextChangeId);
      this.setSearchTextChangeId = null;
    }

    this.setSearchTextChangeId = setTimeout(() => {
      this.setSearchTextChangeId = null;
      this.loadPageFromServer(reason);
    }, delayMs);
  }

  // ----------------------------
  // Main API load
  // ----------------------------
  private loadPageFromServer(reason: string): void {
    this.downloadError = '';
    this.showDetail = false;

    // refresh org (safe)
    const org = localStorage.getItem('organization');
    if (org != null) this.organization = org;

    if (!this.organization || this.organization.trim().length === 0) {
      if (InventoryListingComponent.DEEP_LOGS) {
        console.log('[INV] loadPageFromServer ABORT: organization missing');
      }
      this.source.load([]);
      this.totalPages = 0;
      return;
    }

    let pageUi = Number(this.currentPageNumber || 1);
    if (!Number.isFinite(pageUi) || pageUi < 1) pageUi = 1;
    this.currentPageNumber = pageUi;

    const pageNumberBackend = pageUi - 1;
    const searchText = (this.searchString || '').toString();

    if (InventoryListingComponent.DEEP_LOGS) {
      console.log('[INV] loadPageFromServer reason=', reason, {
        org: this.organization,
        searchText,
        available: this.inventoryAvailable,
        pageUi,
        pageNumberBackend,
        pageSize: this.currentPageSize,
      });
    }

    // ✅ PASS available to API
    this.inventoryService.getAllInventoryOnOrganization(
      this.organization,
      searchText,
      this.inventoryAvailable,
      pageNumberBackend,
      this.currentPageSize
    )
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (resp: any) => {
          if (InventoryListingComponent.DEEP_LOGS) {
            console.log('[INV] API OK pageBackend=', pageNumberBackend, 'resp=', resp);
          }

          const totalRecords = Number(resp?.totalRecords || 0);
          const withAreaRecords = Number(resp?.withAreaRecords || 0);
          const pages = Number(resp?.numberOfPages || 0);
          const rows = (resp?.data || []);

          // ✅ totals + chart only on backend page 0
          if (pageNumberBackend === 0) {
            this.totalPages = pages;

            const noInv = Math.max(0, totalRecords - withAreaRecords);
            this.single = [
              { name: 'Total', value: totalRecords }
            ];

            if (InventoryListingComponent.DEEP_LOGS) {
              console.log('[INV] totals updated (page 1 only)', {
                totalPages: this.totalPages,
                totalRecords,
                withAreaRecords,
                noInv,
              });
            }
          } else {
            if (InventoryListingComponent.DEEP_LOGS) {
              console.log('[INV] totals skipped (not page 1)', { pageNumberBackend });
            }
          }

          // only trust "0 pages" when backend page=0, else keep existing totalPages
          if (pageNumberBackend === 0 && this.totalPages === 0) {
            this.source.load([]);
            return;
          }

          // clamp only if we know totalPages
          if (this.totalPages > 0) {
            if (this.currentPageNumber > this.totalPages) this.currentPageNumber = this.totalPages;
            if (this.currentPageNumber < 1) this.currentPageNumber = 1;
          }

          this.lastPageRows = rows.map((r: any) => this.mapRowForTable(r));
          this.source.load(this.lastPageRows);

          if (InventoryListingComponent.DEEP_LOGS) {
            console.log('[INV] table loaded rows=', this.lastPageRows.length);
          }
        },
        error: err => {
          console.log('[INV] API ERROR', err);
          this.source.load([]);
        }
      });
  }

  private mapRowForTable(r: any): any {
    const firstname = (r?.customerFirstname || '').toString();
    const lastname = (r?.customerLastname || '').toString();

    return {
      __raw: r,

      customerId: r?.customerId ?? '',
      customerName: `${firstname} ${lastname}`.trim(),
      customerPhoneNumber: r?.customerPhoneNumber || '',
      customerCity: r?.customerCity || '',

      propertyType: r?.propertyType || '',
      purpose: r?.purpose || '',

      rent: (r?.rent === true) ? 'true' : 'false',
      rentValue: (r?.rentValue ?? ''),
      bhk: (r?.bhk ?? ''),
      furnishedType: r?.furnishedType || '',
      sqFt: (r?.sqFt ?? ''),
      city: r?.city || '',
      area: r?.area || '',
      callStatus: r?.callStatus || '',
      pid: r?.pid || '',
    };
  }

  onUserRowSelect(event: any): void {
    const row = event?.data;
    if (!row || !row.__raw) return;

    const r = row.__raw;
    this.showDetail = true;

    const kv: { k: string; v: any }[] = [
      { k: 'Customer ID', v: r.customerId },
      { k: 'First Name', v: r.customerFirstname },
      { k: 'Last Name', v: r.customerLastname },
      { k: 'Phone Number', v: r.customerPhoneNumber },
      { k: 'Email', v: r.customerEmail },
      { k: 'Customer City', v: r.customerCity },
      { k: 'Organization', v: r.customerOrganization },

      { k: 'Inventory ID', v: r.id },
      { k: 'Premise Name', v: r.premiseName },
      { k: 'Listed Date', v: r.listedDate },
      { k: 'Property Type', v: r.propertyType },
      { k: 'Purpose', v: r.purpose },

      { k: 'Rent', v: r.rent },
      { k: 'Rent Value', v: r.rentValue },
      { k: 'BHK', v: r.bhk },
      { k: 'Furnished Type', v: r.furnishedType },
      { k: 'SqFt', v: r.sqFt },
      { k: 'Nearby', v: r.nearby },
      { k: 'Area', v: r.area },
      { k: 'Inventory City', v: r.city },
      { k: 'Call Status', v: r.callStatus },
      { k: 'Property Age', v: r.propertyAge },
      { k: 'Unit Type', v: r.unitType },
      { k: 'Tenant', v: r.tenant },
      { k: 'Facing', v: r.facing },
      { k: 'Total Floors', v: r.totalFloors },
      { k: 'Brokerage', v: r.brokerage },
      { k: 'Balconies', v: r.balconies },
      { k: 'Washroom', v: r.washroom },
      { k: 'Unit No', v: r.unitNo },
      { k: 'Floor No', v: r.floorNo },
      { k: 'PID', v: r.pid },
      { k: 'Description', v: r.propertyDescription1 },
      { k: 'More Than One Property', v: r.moreThanOneProperty },
      { k: 'Created On', v: r.createdOn },
      { k: 'Last Updated On', v: r.lastUpdatedOn },
    ];

    this.keys = kv.map(x => x.k);
    this.values = kv.map(x => (x.v === null || x.v === undefined || x.v === '') ? 'N/A' : x.v);
  }

  downloadInventoryExcel(): void {
    this.downloadError = '';

    if (!this.fromListedDateLocal) {
      this.downloadError = 'Please select a date.';
      return;
    }

    // ensure org is present
    const org = localStorage.getItem('organization');
    if (org != null) this.organization = org;

    if (!this.organization || this.organization.trim().length === 0) {
      this.downloadError = 'Organization not loaded. Please refresh.';
      return;
    }

    const iso = this.fromListedDateLocal.toISOString();

    if (InventoryListingComponent.DEEP_LOGS) {
      console.log('[INV] downloadInventoryExcel iso=', iso, 'org=', this.organization, 'available=', this.inventoryAvailable);
    }

    // ✅ PASS available to Excel API
    this.inventoryService.fetchExcelAfterListedDate(this.organization, iso, this.inventoryAvailable)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blobResp: any) => {
          try {
            const blob = blobResp as Blob;
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;

            const safeIso = iso.replace(/[:.]/g, '-');
            a.download = `inventory_${this.organization}_${safeIso}.xlsx`;

            a.click();
            window.URL.revokeObjectURL(url);
          } catch (e) {
            console.log(e);
            this.downloadError = 'Download failed.';
          }
        },
        error: err => {
          console.log('[INV] Excel download error', err);
          this.downloadError = 'Download failed.';
        }
      });
  }
}

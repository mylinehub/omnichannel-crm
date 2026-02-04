// ============================================================
// FILE: run-campaign-view.component.ts
// ============================================================
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, takeWhile } from 'rxjs/operators';
import { LocalDataSource } from 'ng2-smart-table';
import { NbThemeService } from '@nebular/theme';

import { ConstantsService } from '../../../service/constants/constants.service';

// OPTIONAL: if you want campaign details card from existing campaign APIs
import { CustomInputTableComponent } from '../../employee/all-employees/custom-input-table/custom-input-table.component';
import { RunCampaignService } from '../service/run-campaign-service/run-campaign.service';
import { CampaignService } from '../service/campaign.service';

@Component({
  selector: 'ngx-run-campaign-view',
  templateUrl: './run-campaign-view.component.html',
  styleUrls: ['./run-campaign-view.component.scss']
})
export class RunCampaignViewComponent implements OnInit, OnDestroy {

  private static readonly DEEP_LOGS = true;

  // mimic customers/inventory gating
  private static readonly CONTEXT_WAIT_MAX_RETRIES = 20;     // 20 * 300ms = 6s
  private static readonly CONTEXT_WAIT_INTERVAL_MS = 300;

  private destroy$: Subject<void> = new Subject<void>();
  private alive = true;

  // org
  organization = '';

  // dropdowns
  campaignIds: string[] = [];   // backend returns ["123 - Name", ...]
  runIds: string[] = [];        // backend returns ["999 - 2025-12-26 10:10:10", ...]

  // IMPORTANT:
  // Nebular nb-select binds the "selected" value by strict equality.
  // Since options are strings ("123 - Name"), keep UI selection as string,
  // and keep parsed numeric IDs separately for API calls.
  selectedCampaignUi: string | null = null;
  selectedRunUi: string | null = null;

  selectedCampaignId: number | null = null;   // numeric, used for API
  selectedRunId: number | null = null;        // numeric, used for API

  // campaign info card (optional)
  campaignInfo: any = null;
  campaignInfoError: string = '';

  // ===== Current run table state =====
  currentSource: LocalDataSource = new LocalDataSource();
  currentSearchString: string = '';

  // UI strings (allow empty while typing)
  currentPageNumberUi: string = '1';
  currentPageSizeUi: string = '10';

  // numeric values used for API
  private currentPageNumber: number = 1; // UI 1-based
  private currentPageSize: number = 10;

  currentTotalPages: number = 0;
  currentLoadError: string = '';
  private currentDebounceId: any = null;

  // ===== Selected run table state =====
  selectedSource: LocalDataSource = new LocalDataSource();
  selectedSearchString: string = '';

  // UI strings (allow empty while typing)
  selectedPageNumberUi: string = '1';
  selectedPageSizeUi: string = '10';

  // numeric values used for API
  private selectedPageNumber: number = 1; // UI 1-based
  private selectedPageSize: number = 10;

  selectedTotalPages: number = 0;
  selectedLoadError: string = '';
  private selectedDebounceId: any = null;

  // context wait
  private contextWaitTimer: any = null;
  private contextWaitRetryCount = 0;
  private initialLoaded = false;

  isWhatsappCampaign: boolean = false;
  currentLoading: boolean = false;

  // theme (optional if you want chart later)
  colorScheme: any;
  themeSubscription: any;

  // table settings (same columns for both)
  currentTableSettings = this.buildTableSettings({ includeCost: false });
  selectedTableSettings = this.buildTableSettings({ includeCost: true });

  // ===== Status summary (Selected Run - DB) =====
  selectedCountsMap: Record<string, number> = {};
  selectedCountsList: Array<{ key: string; value: number }> = [];
  runNumber: number = 0;
  selectedCampaignName: string = '';
  selectedCampaignRunDate: string = '';
  selectedTotalCost: number = 0;
  selectedTotalDialed: number = 0;

  @ViewChild('currentAutoPageNumber') currentPageNumberInput: any;
  @ViewChild('currentSearchName') currentSearchTextInput: any;

  @ViewChild('selectedAutoPageNumber') selectedPageNumberInput: any;
  @ViewChild('selectedSearchName') selectedSearchTextInput: any;

  constructor(
    private runCampaignService: RunCampaignService,
    private themeService: NbThemeService,
    private campaignService: CampaignService, // optional usage, safe even if you comment out calls
  ) {
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
    if (RunCampaignViewComponent.DEEP_LOGS) {
      console.log('[RUN] ngOnInit org(localStorage)=', this.organization, 'user=', ConstantsService?.user);
    }
    this.waitForContextAndLoad();
  }

  ngOnDestroy(): void {
    this.alive = false;

    if (this.currentDebounceId != null) clearTimeout(this.currentDebounceId);
    if (this.selectedDebounceId != null) clearTimeout(this.selectedDebounceId);
    if (this.contextWaitTimer != null) clearTimeout(this.contextWaitTimer);

    this.destroy$.next();
    this.destroy$.complete();

    if (this.themeSubscription) this.themeSubscription.unsubscribe();
  }

  // ----------------------------
  // Context wait (like Inventory)
  // ----------------------------
  private waitForContextAndLoad(): void {
    const org = localStorage.getItem('organization');
    if (org != null) this.organization = org;

    const orgReady = !!(this.organization && this.organization.trim().length > 0);
    const userReady = (ConstantsService?.user && ConstantsService.user.firstName !== undefined);

    if (RunCampaignViewComponent.DEEP_LOGS) {
      console.log('[RUN] waitForContextAndLoad retry=', this.contextWaitRetryCount, {
        org: this.organization,
        orgReady,
        userReady,
      });
    }

    if (orgReady && userReady) {
      if (!this.initialLoaded) {
        this.initialLoaded = true;
        this.loadCampaignIds('initialContextReady');
      }
      return;
    }

    if (this.contextWaitRetryCount >= RunCampaignViewComponent.CONTEXT_WAIT_MAX_RETRIES) {
      if (RunCampaignViewComponent.DEEP_LOGS) {
        console.log('[RUN] context NOT ready after retries. Will not auto-load.');
      }
      return;
    }

    this.contextWaitRetryCount++;
    this.contextWaitTimer = setTimeout(
      () => this.waitForContextAndLoad(),
      RunCampaignViewComponent.CONTEXT_WAIT_INTERVAL_MS
    );
  }

  // ----------------------------
  // Dropdown handlers
  // ----------------------------
  onCampaignChange(newVal: any): void {
    // keep UI selection as-is (string like "123 - Name")
    this.selectedCampaignUi = (newVal === null || newVal === undefined || String(newVal).trim() === '') ? null : String(newVal);

    // parse numeric id for APIs
    const id = this.parseIdFromDisplay(newVal);
    this.selectedCampaignId = id;

    // reset run selection
    this.selectedRunUi = null;
    this.selectedRunId = null;
    this.runIds = [];

    this.resetCurrentTable();
    this.resetSelectedTable();

    this.campaignInfo = null;
    this.campaignInfoError = '';

    if (this.selectedCampaignId == null) return;

    this.loadRunIdsForCampaign('campaignChange');

    this.currentSearchString = '';
    this.currentPageNumber = 1;
    this.currentPageNumberUi = '1';

    this.loadCurrentRunTable('campaignChange');
    this.loadCampaignInfoSafe('campaignChange');
  }

  onRunChange(newVal: any): void {
    // keep UI selection as-is (string like "999 - 2025-12-26 10:10:10")
    this.selectedRunUi = (newVal === null || newVal === undefined || String(newVal).trim() === '') ? null : String(newVal);

    // parse numeric id for APIs
    const id = this.parseIdFromDisplay(newVal);
    this.selectedRunId = id;

    this.resetSelectedTable();

    if (this.selectedCampaignId == null || this.selectedRunId == null) return;

    this.selectedSearchString = '';
    this.selectedPageNumber = 1;
    this.selectedPageNumberUi = '1';

    this.loadSelectedRunTable('runChange');
  }

  // ----------------------------
  // API Loads
  // ----------------------------
  private loadCampaignIds(reason: string): void {
    this.clearErrors();
    if (!this.organization) return;

    this.runCampaignService.listCampaignIdsMerged(this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (resp: any) => {
          const arr = (resp || []) as string[];
          this.campaignIds = [...arr];
        },
        error: (err) => {
          console.log('[RUN] listCampaignIdsMerged ERROR', err);
          this.campaignIds = [];
        }
      });
  }

  private loadRunIdsForCampaign(reason: string): void {
    this.clearErrors();
    if (!this.organization || !this.selectedCampaignId) return;

    this.runCampaignService.listRunIdsForCampaignMerged(this.organization, this.selectedCampaignId!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (resp: any) => {
          const arr = (resp || []) as string[];
          this.runIds = [...arr];
        },
        error: (err) => {
          console.log('[RUN] listRunIdsForCampaignMerged ERROR', err);
          this.runIds = [];
        }
      });
  }

  // CURRENT RUN TABLE (memory-only in your controller/service)
  private loadCurrentRunTable(reason: string): void {
    this.currentLoadError = '';

    if (!this.organization || !this.selectedCampaignId) {
      this.currentLoading = false;
      this.resetCurrentTable();
      return;
    }
    this.currentLoading = true;

    // NOTE: pageBackend/size are not used by current memory-only API (kept for logs consistency)
    const pageBackend = Math.max(0, (this.currentPageNumber || 1) - 1);
    const searchText = (this.currentSearchString || '').toString();
    const campaignIdNum = this.selectedCampaignId;
    const size = Math.max(1, this.currentPageSize || 10);

    if (RunCampaignViewComponent.DEEP_LOGS) {
      console.log('[RUN] loadCurrentRunTable reason=', reason, {
        campaignId: campaignIdNum,
        pageBackend,
        size,
        searchText
      });
    }

    this.runCampaignService.getCurrentRunLiveLogsMemoryOnly(
      this.organization,
      campaignIdNum,
      searchText
    )
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (resp: any) => {
          const rows = (resp?.data || []);

          // update pages ONLY when UI page is 1
          if (this.currentPageNumber === 1) {
            const pages = Number(resp?.numberOfPages || 0);
            this.currentTotalPages = pages;
          }

          // clamp numeric page if needed
          if (this.currentTotalPages > 0) {
            if (this.currentPageNumber > this.currentTotalPages) {
              this.currentPageNumber = this.currentTotalPages;
              this.currentPageNumberUi = String(this.currentPageNumber);
            }
            if (this.currentPageNumber < 1) {
              this.currentPageNumber = 1;
              this.currentPageNumberUi = '1';
            }
          } else {
            this.currentPageNumber = 1;
            this.currentPageNumberUi = '1';
          }

          this.runNumber = Number(resp?.runNumber ?? 0);
          const mapped = rows.map((r: any) => this.mapLogRow(r));
          this.currentSource.load(mapped);
          this.currentLoading = false;
        },
        error: (err) => {
          console.log('[RUN] getCallLogsMergedForCurrentRun ERROR', err);
          this.currentLoadError = 'Failed to load current run logs.';
          this.currentSource.load([]);
          this.currentTotalPages = 0;
          this.currentLoading = false;
        }
      });
  }

  // SELECTED RUN TABLE (DB-only)
  private loadSelectedRunTable(reason: string): void {
    this.selectedLoadError = '';

    if (!this.organization || !this.selectedCampaignId || !this.selectedRunId) {
      this.resetSelectedTable();
      return;
    }

    const pageBackend = Math.max(0, (this.selectedPageNumber || 1) - 1);
    const searchText = (this.selectedSearchString || '').toString();

    const campaignIdNum = this.selectedCampaignId!;
    const runIdNum = this.selectedRunId!;
    const size = Math.max(1, this.selectedPageSize || 10);

    if (RunCampaignViewComponent.DEEP_LOGS) {
      console.log('[RUN] loadSelectedRunTable reason=', reason, {
        campaignId: campaignIdNum,
        runId: runIdNum,
        pageBackend,
        size,
        searchText
      });
    }

    this.runCampaignService.getCallLogsMergedForRun(
      this.organization,
      campaignIdNum,
      runIdNum,
      pageBackend,
      size,
      searchText
    )
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (resp: any) => {
          const rows = (resp?.data || []);

          // update pages ONLY when UI page is 1
          if (this.selectedPageNumber === 1) {
            const pages = Number(resp?.numberOfPages || 0);
            this.selectedTotalPages = pages;
          }

          // clamp numeric page if needed
          if (this.selectedTotalPages > 0) {
            if (this.selectedPageNumber > this.selectedTotalPages) {
              this.selectedPageNumber = this.selectedTotalPages;
              this.selectedPageNumberUi = String(this.selectedPageNumber);
            }
            if (this.selectedPageNumber < 1) {
              this.selectedPageNumber = 1;
              this.selectedPageNumberUi = '1';
            }
          } else {
            this.selectedPageNumber = 1;
            this.selectedPageNumberUi = '1';
          }

          this.selectedCampaignName = String(resp?.campaignName ?? '');
          this.selectedCampaignRunDate = String(resp?.campaignRunDate ?? '');
          this.selectedTotalCost = Number(resp?.totalCost ?? 0);
          this.selectedTotalDialed = Number(resp?.totalDialed ?? 0);

          const mapped = rows.map((r: any) => this.mapLogRow(r));
          this.buildCountsFromResponse(resp);
          this.selectedSource.load(mapped);
        },
        error: (err) => {
          console.log('[RUN] getCallLogsMergedForRun ERROR', err);
          this.selectedLoadError = 'Failed to load selected run logs.';
          this.selectedSource.load([]);
          this.selectedTotalPages = 0;
          this.selectedCountsMap = {};
          this.selectedCountsList = [];

        }
      });
  }

  // ----------------------------
  // Campaign info card (optional)
  // ----------------------------
  private loadCampaignInfoSafe(reason: string): void {
    this.campaignInfo = null;
    this.campaignInfoError = '';

    if (!this.organization || !this.selectedCampaignId) return;

    const campaignIdNum = this.selectedCampaignId;
    if (!Number.isFinite(campaignIdNum)) return;

    if (RunCampaignViewComponent.DEEP_LOGS) {
      console.log('[RUN] loadCampaignInfoSafe reason=', reason, { campaignIdNum });
    }

    const anyService: any = this.campaignService as any;
    if (!anyService?.getCampaignByIdAndOrganization) {
      this.campaignInfoError = '';
      return;
    }

    anyService.getCampaignByIdAndOrganization(campaignIdNum, this.organization)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (resp: any) => {
          this.campaignInfo = resp || null;

          const t = (this.campaignInfo?.autodialertype || '').toString().toLowerCase();
          this.isWhatsappCampaign = t.includes('whatsapp');

          // IMPORTANT: force ng2-smart-table to pick new column title
          this.currentTableSettings = this.buildTableSettings({ includeCost: false });
          this.selectedTableSettings = this.buildTableSettings({ includeCost: true });
        },
        error: (err: any) => {
          console.log('[RUN] getCampaignByIdAndOrganization ERROR', err);
          this.campaignInfoError = 'Campaign details not available.';
          this.campaignInfo = null;
          this.isWhatsappCampaign = false;
          this.currentTableSettings = this.buildTableSettings({ includeCost: false });
          this.selectedTableSettings = this.buildTableSettings({ includeCost: true });
        }
      });
  }

  // ----------------------------
  // Search controls
  // ----------------------------
  onCurrentSearchTextChange(): void {
    if (!this.initialLoaded) return;

    this.currentPageNumber = 1;
    this.currentPageNumberUi = '1';

    this.debounce('current', 700, 'searchChange');
  }

  onSelectedSearchTextChange(): void {
    if (!this.initialLoaded) return;

    this.selectedPageNumber = 1;
    this.selectedPageNumberUi = '1';

    this.debounce('selected', 700, 'searchChange');
  }

  // ----------------------------
  // Page number controls (allow empty; only call API when valid)
  // ----------------------------
  onCurrentPageNumberChange(): void {
    if (!this.initialLoaded) return;

    const n = this.parsePositiveIntOrNull(this.currentPageNumberUi);
    if (n === null) return;

    if (this.currentTotalPages > 0 && n > this.currentTotalPages) {
      this.currentPageNumber = this.currentTotalPages;
      this.currentPageNumberUi = String(this.currentPageNumber);
      return;
    }

    this.currentPageNumber = n;
    this.debounce('current', 400, 'pageChange');
  }

  onSelectedPageNumberChange(): void {
    if (!this.initialLoaded) return;

    const n = this.parsePositiveIntOrNull(this.selectedPageNumberUi);
    if (n === null) return;

    if (this.selectedTotalPages > 0 && n > this.selectedTotalPages) {
      this.selectedPageNumber = this.selectedTotalPages;
      this.selectedPageNumberUi = String(this.selectedPageNumber);
      return;
    }

    this.selectedPageNumber = n;
    this.debounce('selected', 400, 'pageChange');
  }

  onCurrentPageNumberBlur(): void {
    if (!this.initialLoaded) return;

    const n = this.parsePositiveIntOrNull(this.currentPageNumberUi);
    if (n === null) {
      this.currentPageNumber = 1;
      this.currentPageNumberUi = '1';
      this.loadCurrentRunTable('pageBlurDefault');
    }
  }

  onSelectedPageNumberBlur(): void {
    if (!this.initialLoaded) return;

    const n = this.parsePositiveIntOrNull(this.selectedPageNumberUi);
    if (n === null) {
      this.selectedPageNumber = 1;
      this.selectedPageNumberUi = '1';
      this.loadSelectedRunTable('pageBlurDefault');
    }
  }

  // ----------------------------
  // Page size controls (allow empty; only call API when valid)
  // ----------------------------
  onCurrentPageSizeChange(): void {
    if (!this.initialLoaded) return;

    const n = this.parsePositiveIntOrNull(this.currentPageSizeUi);
    if (n === null) return;

    this.currentPageSize = n;

    this.currentPageNumber = 1;
    this.currentPageNumberUi = '1';

    this.loadCurrentRunTable('currentPageSizeChange');
  }

  onSelectedPageSizeChange(): void {
    if (!this.initialLoaded) return;

    const n = this.parsePositiveIntOrNull(this.selectedPageSizeUi);
    if (n === null) return;

    this.selectedPageSize = n;

    this.selectedPageNumber = 1;
    this.selectedPageNumberUi = '1';

    this.loadSelectedRunTable('selectedPageSizeChange');
  }

  onCurrentPageSizeBlur(): void {
    if (!this.initialLoaded) return;

    const n = this.parsePositiveIntOrNull(this.currentPageSizeUi);
    if (n === null) {
      this.currentPageSize = 10;
      this.currentPageSizeUi = '10';
      this.currentPageNumber = 1;
      this.currentPageNumberUi = '1';
      this.loadCurrentRunTable('pageSizeBlurDefault');
    }
  }

  onSelectedPageSizeBlur(): void {
    if (!this.initialLoaded) return;

    const n = this.parsePositiveIntOrNull(this.selectedPageSizeUi);
    if (n === null) {
      this.selectedPageSize = 10;
      this.selectedPageSizeUi = '10';
      this.selectedPageNumber = 1;
      this.selectedPageNumberUi = '1';
      this.loadSelectedRunTable('pageSizeBlurDefault');
    }
  }

  // ----------------------------
  // Input constraint: digits only (no API here)
  // ----------------------------
  verifyNumericConstraint(event: KeyboardEvent): void {
    const allowed = ['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Tab', 'Home', 'End'];
    if (allowed.includes(event.key)) return;

    if (!/^\d$/.test(event.key)) {
      event.preventDefault();
    }
  }

  private parsePositiveIntOrNull(v: string): number | null {
    if (v === null || v === undefined) return null;
    const s = String(v).trim();
    if (s.length === 0) return null;
    if (!/^\d+$/.test(s)) return null;
    const n = Number(s);
    if (!Number.isFinite(n) || n < 1) return null;
    return Math.floor(n);
  }

  private debounce(table: 'current' | 'selected', delayMs: number, reason: string): void {
    if (table === 'current') {
      if (this.currentDebounceId != null) clearTimeout(this.currentDebounceId);
      this.currentDebounceId = setTimeout(() => {
        this.currentDebounceId = null;
        this.loadCurrentRunTable(reason);
      }, delayMs);
    } else {
      if (this.selectedDebounceId != null) clearTimeout(this.selectedDebounceId);
      this.selectedDebounceId = setTimeout(() => {
        this.selectedDebounceId = null;
        this.loadSelectedRunTable(reason);
      }, delayMs);
    }
  }

  downloadSelectedRunExcel(): void {
    if (!this.selectedCampaignId || !this.selectedRunId) return;

    const campaignId = this.selectedCampaignId;
    const runId = this.selectedRunId;

    this.runCampaignService.exportRunExcelDbOnly(this.organization, campaignId, runId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blobResp: any) => {
          const blob: Blob = blobResp as Blob;

          const ts = new Date().toISOString().replace(/[:.]/g, '-');
          const filename = `campaign_${campaignId}_run_${runId}_${ts}.xlsx`;

          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = filename;
          document.body.appendChild(a);
          a.click();
          a.remove();
          window.URL.revokeObjectURL(url);
        },
        error: (err) => {
          console.log('[RUN] exportRunExcelDbOnly ERROR', err);
          this.selectedLoadError = 'Failed to download Excel.';
        }
      });
  }


  downloadSelectedRunRecording(): void {
    if (!this.selectedCampaignId || !this.selectedRunId) return;

    const campaignId = this.selectedCampaignId;
    const runId = this.selectedRunId;

    this.runCampaignService.exportRunRecordings(this.organization, campaignId, runId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blobResp: any) => {
          const blob: Blob = blobResp as Blob;

          const ts = new Date().toISOString().replace(/[:.]/g, '-');
          const filename = `campaign_${campaignId}_recordings_${runId}_${ts}.zip`;

          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = filename;
          document.body.appendChild(a);
          a.click();
          a.remove();
          window.URL.revokeObjectURL(url);
        },
        error: (err) => {
          console.log('[RUN] exportRunRecording ERROR', err);
          this.selectedLoadError = 'Failed to download recordings.';
        }
      });
  }

  // ----------------------------
  // Helpers
  // ----------------------------
  private parseIdFromDisplay(v: any): number | null {
    if (v === null || v === undefined) return null;

    const s = String(v).trim();
    if (!s) return null;

    // "123 - Name", "123-Name", "123"
    const m = s.match(/^(\d+)/);
    if (!m) return null;

    const n = Number(m[1]);
    return Number.isFinite(n) ? n : null;
  }

  private buildTableSettings(opts?: { includeCost?: boolean }): any {
    const includeCost = opts?.includeCost === true;

    const columns: any = {
      eventAt: { title: 'Event At', type: 'string' },
      toNumber: { title: 'Customer Phone', type: 'string' },
      callState: { title: this.getStateColumnTitle(), type: 'string' },
    };

    // Cost only for Selected Run (DB), NOT for Current Run (Live)
    if (includeCost) {
      columns.callCost = { title: 'Cost (INR)', type: 'number' };
    }

    // duration ONLY for non-WhatsApp campaigns
    if (!this.isWhatsappCampaign) {
      columns.durationMs = { title: 'Duration (ms)', type: 'number' };
    }

    return {
      actions: { add: false, edit: false, delete: false },
      hideSubHeader: true,
      pager: { display: false },
      columns,
    };
  }


  private mapLogRow(r: any): any {
    return {
      eventAt: this.formatEventAt(r?.eventAt) ?? '',
      toNumber: r?.toNumber ?? '',
      callState: r?.callState ?? '',
      callCost: r?.callCost ?? 0,
      durationMs: r?.durationMs ?? '',
    };
  }

  private resetCurrentTable(): void {
    this.currentSource.load([]);
    this.currentTotalPages = 0;
    this.currentLoadError = '';
    this.currentSearchString = '';

    this.currentPageNumber = 1;
    this.currentPageNumberUi = '1';

    this.currentPageSize = 10;
    this.currentPageSizeUi = '10';
    this.runNumber = 0;
  }

  private resetSelectedTable(): void {
    this.selectedSource.load([]);
    this.selectedTotalPages = 0;
    this.selectedLoadError = '';
    this.selectedSearchString = '';

    this.selectedPageNumber = 1;
    this.selectedPageNumberUi = '1';

    this.selectedPageSize = 10;
    this.selectedPageSizeUi = '10';
    this.selectedCountsMap = {};
    this.selectedCountsList = [];
    this.selectedCampaignName = '';
    this.selectedCampaignRunDate = '';
    this.selectedTotalCost = 0;
    this.selectedTotalDialed = 0;
  }

  hasSelectedCountsOrData(): boolean {
    return this.selectedCampaignId != null &&
          this.selectedRunId != null &&
          this.selectedCountsList.length > 0;
  }


  private clearErrors(): void {
    this.currentLoadError = '';
    this.selectedLoadError = '';
    this.campaignInfoError = '';
  }

  private formatEventAt(v: any): string {
    if (v === null || v === undefined) return '';

    let ms: number | null = null;

    // Java Instant-like: { epochSecond, nano }
    if (typeof v === 'object' && v.epochSecond !== undefined) {
      const sec = Number(v.epochSecond);
      const nano = Number(v.nano ?? 0);
      if (Number.isFinite(sec)) {
        ms = sec * 1000 + Math.floor(nano / 1_000_000);
      }
    }
    // raw number (epoch seconds or milliseconds)
    else if (typeof v === 'number' && Number.isFinite(v)) {
      ms = v < 10_000_000_000 ? v * 1000 : v;
    }

    if (ms === null) return '';

    const d = new Date(ms);

    const pad = (n: number) => String(n).padStart(2, '0');

    const dd = pad(d.getDate());
    const MM = pad(d.getMonth() + 1);
    const yy = String(d.getFullYear()).slice(-2);
    const HH = pad(d.getHours());
    const mm = pad(d.getMinutes());
    const ss = pad(d.getSeconds());

    return `${dd}-${MM}-${yy} ${HH}:${mm}:${ss}`;
  }

  private getStateColumnTitle(): string {
    return this.isWhatsappCampaign ? 'Delivery State' : 'Call State';
  }

  refreshCurrentLiveTable(): void {
    this.loadCurrentRunTable('manualRefresh');
  }

 private buildCountsFromResponse(resp: any): void {
    const raw = (resp?.stateCounts || {}) as Record<string, any>;

    const map: Record<string, number> = {};
    for (const [k, v] of Object.entries(raw)) {
      const key = String(k || '').trim().toUpperCase();
      if (!key) continue;

      const num = Number(v ?? 0);
      map[key] = Number.isFinite(num) ? num : 0;
    }

    this.selectedCountsMap = map;

    // list for UI
    this.selectedCountsList = Object.entries(map)
      .map(([key, value]) => ({ key, value }));

  }


}

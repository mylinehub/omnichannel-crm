import { Injectable } from '@angular/core';
import { ConstantsService } from '../../../service/constants/constants.service';
import { ApiHttpService } from '../../../service/http/api-http.service';

@Injectable({
  providedIn: 'root'
})
export class FranchiseManagementService {

  constructor(
    protected constService: ConstantsService,
    protected httpService: ApiHttpService,
  ) { }

  // List / Search / Paging
  public getAllFranchiseInventoryOnOrganization(
    organization: string,
    searchText: string,
    available: boolean,
    pageNumber: number,
    size: number
  ) {

    let endpoint: string = this.constService.API_GETALL_FRANCHISE_INVENTORY_ENDPOINT;

    endpoint = endpoint.replace('{{organization}}', organization);
    endpoint = endpoint.replace('{{searchText}}', String(searchText || ''));
    endpoint = endpoint.replace('{{available}}', String(available));
    endpoint = endpoint.replace('{{pageNumber}}', String(pageNumber));
    endpoint = endpoint.replace('{{size}}', String(size));

    return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint);
  }

  // Get single row by customerId
  public getFranchiseInventoryByCustomerOnOrganization(
    organization: string,
    customerId: string
  ) {

    let endpoint: string = this.constService.API_GET_FRANCHISE_INVENTORY_BY_CUSTOMER_ENDPOINT;

    endpoint = endpoint.replace('{{organization}}', organization);
    endpoint = endpoint.replace('{{customerId}}', String(customerId));

    return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint);
  }

  // Get single row by id
  public getFranchiseInventoryByIdOnOrganization(
    organization: string,
    id: string
  ) {

    let endpoint: string = this.constService.API_GET_FRANCHISE_INVENTORY_BY_ID_ENDPOINT;

    endpoint = endpoint.replace('{{organization}}', organization);
    endpoint = endpoint.replace('{{id}}', String(id));

    return this.httpService.getWithTokenHandleded(this.constService.API_BASE_ENDPOINT + endpoint);
  }

  public fetchFranchiseExcelAfterCreatedDate(
    organization: string,
    fromCreatedDateIso: string,
    available: boolean
  ) {

    let endpoint: string = this.constService.API_DOWNLOAD_FRANCHISE_INVENTORY_ENDPOINT;
    endpoint = endpoint.replace('{{organization}}', organization);
    endpoint = endpoint.replace('{{fromCreatedDateIso}}', String(fromCreatedDateIso));
    endpoint = endpoint.replace('{{available}}', String(available));

    return this.httpService.getWithTokenAndBlobAsJson(this.constService.API_BASE_ENDPOINT + endpoint);
  }
}

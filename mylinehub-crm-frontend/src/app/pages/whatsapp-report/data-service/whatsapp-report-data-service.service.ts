import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WhatsappReportDataServiceService {

  filteredOptions$: Observable<string[]>;

  constructor() { }
}

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { Subject, takeUntil } from 'rxjs';
import { FileStorageService } from '../service/file-storage.service';

@Component({
  selector: 'ngx-upload-status',
  templateUrl: './upload-status.component.html',
  // styleUrls: ['./upload-status.component.scss']
  styles: [`
            .container {
              display: flex;
              align-items: center;
            }

            nb-progress-bar {
              flex: 1;
            }
          `],
})
export class UploadStatusComponent implements OnInit {

  private destroy$: Subject<void> = new Subject<void>();
  @Input() fileStorageService: FileStorageService;

  constructor(protected ref: NbDialogRef<UploadStatusComponent>,) { }

  ngOnInit(): void {
    this.fileStorageService.isFileUploadDialogOpen = true;
  }


  dismiss(value:String) {
    this.ref.close(value);
  }

  cancel()
  {
    if ( this.fileStorageService.uploadSubscription ) {
      this.fileStorageService.uploadSubscription.unsubscribe();
      this.fileStorageService.fileUploadStatus.status = 'done';
      this.fileStorageService.isFileUploadDialogOpen = false;
      this.fileStorageService.currentFileSize = this.fileStorageService.currentFileSize - this.fileStorageService.totalUploadedFiles;
      for(let i =0 ; i < this.fileStorageService.totalUploadedFiles ; i++)
        {
              this.fileStorageService.allFiles.shift();
              this.fileStorageService.filteredOptionsList.shift();
        }
    }
    this.ref.close('');
  }

  // setValue(newValue) {
  //   this.fileStorageService.fileUploadStatus.percent = Math.min(Math.max(newValue, 0), 100)
  // }

  get status() {
    if (this.fileStorageService.fileUploadStatus.percent <= 25) {
      return 'danger';
    } else if (this.fileStorageService.fileUploadStatus.percent <= 50) {
      return 'warning';
    } else if (this.fileStorageService.fileUploadStatus.percent <= 75) {
      return 'info';
    } else {
      return 'success';
    }
  }

}


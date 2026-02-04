import { Component, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { FileStorageService } from '../service/file-storage.service';


@Component({
  selector: 'ngx-download-status',
  templateUrl: './download-status.component.html',
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
export class DownloadStatusComponent implements OnInit {

  @Input() fileStorageService: FileStorageService;

  constructor(protected ref: NbDialogRef<DownloadStatusComponent>,) { }

  ngOnInit(): void {
    this.fileStorageService.isFileDownloadDialogOpen = true;
  }

  cancel()
  {
    if ( this.fileStorageService.downloadSubscription ) {
      this.fileStorageService.downloadSubscription.unsubscribe();
      this.fileStorageService.multipleFileDownloadStatus.status = 'done';
      this.fileStorageService.isFileDownloadDialogOpen = false;
    }
    this.ref.close('');
  }

  dismiss(value:String) {
    this.ref.close(value);
  }


  get status() {
    if (this.fileStorageService.multipleFileDownloadStatus.percent <= 25) {
      return 'danger';
    } else if (this.fileStorageService.multipleFileDownloadStatus.percent <= 50) {
      return 'warning';
    } else if (this.fileStorageService.multipleFileDownloadStatus.percent <= 75) {
      return 'info';
    } else {
      return 'success';
    }
  }

}



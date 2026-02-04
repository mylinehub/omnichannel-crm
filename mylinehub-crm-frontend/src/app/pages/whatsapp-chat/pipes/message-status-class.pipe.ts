import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'messageStatusClass',
  pure: false // to detect internal property changes reactively
})
export class MessageStatusClassPipe implements PipeTransform {
  transform(data: any): string {
    if (data?.deleted) {
      return 'status-deleted';
    } else if (data?.failed) {
      return 'status-failed';
    } else if (data?.read) {
      return 'status-read';
    } else if (data?.delivered) {
      return 'status-delivered';
    } else if (data?.send) {
      return 'status-sent';
    } else {
      return 'status-pending';
    }
  }
}

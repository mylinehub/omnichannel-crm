import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'messageStatus',
  pure: false // important: make pipe impure to detect property changes
})
export class MessageStatusPipe implements PipeTransform {
  transform(data: any): string {
    if (data?.deleted) {
      return '🗑️ Deleted';
    } else if (data?.failed) {
      return '❌ Failed';
    } else if (data?.read) {
      return '✅✅ Read';
    } else if (data?.delivered) {
      return '✅ Delivered';
    } else if (data?.send) {
      return '📤 Sent';
    } else {
      return '⏳ Pending';
    }
  }
}

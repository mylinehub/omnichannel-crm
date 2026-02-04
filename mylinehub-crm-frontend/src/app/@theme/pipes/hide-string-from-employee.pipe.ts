import { Pipe, PipeTransform } from '@angular/core';
import { ConstantsService } from '../../service/constants/constants.service';

@Pipe({
  name: 'hideStringFromEmployee'
})
export class HideStringFromEmployeePipe implements PipeTransform {

  transform(value: string): string {

   console.log('Executing HideStringFromEmployeePipe');
   let returnValue = '';

   if(ConstantsService.user.role === ConstantsService.employee)
    {
       const len = value.length;

        if (len <= 5) {
          returnValue = value; // No characters to mask if too short
        }
        else{
            const firstThree = value.slice(0, 3);
            const lastTwo = value.slice(-2);
            const maskedLength = len - 5;
            const maskedPart = '*'.repeat(maskedLength);

            returnValue =  firstThree + maskedPart + lastTwo;
        }
    }
    else{
      returnValue = value;
    }

    return returnValue;
  }

}

import { Component, OnInit } from '@angular/core';
import { ConstantsService } from '../../service/constants/constants.service';
import { Router } from '@angular/router';

@Component({
  selector: 'ngx-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {

  redirectDelay: number = 0;
  
  constructor(protected constantService : ConstantsService, protected router: Router,) { }

  ngOnInit(): void {

    setTimeout(() => {
      return this.router.navigateByUrl(this.constantService.RESET_PASSWORD_ENDPOINT);
    }, this.redirectDelay);

  }

}

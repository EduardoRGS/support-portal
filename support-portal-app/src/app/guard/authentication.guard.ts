import { NotificationService } from './../service/notification.service';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { AuthenticationService } from '../service/authentication.service';
import { NotificationType } from '../enum/notification-type.enum';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationGuard implements CanActivate {
  
  constructor( private authenticationService: AuthenticationService, private router: Router,
               private notificationService: NotificationService) {}
  
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    return this.isUserLoggedIn();
  }

  private isUserLoggedIn(): boolean {
    if( this.authenticationService.isUserLoggedIn()){
      return true;
    }
    this.router.navigate(['login']);
    this.notificationService.showNotification(NotificationType.ERROR, 'Você precisa está logado para ter acesso a esta página');
    return false;
  }
  
}

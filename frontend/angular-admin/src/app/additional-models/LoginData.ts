export interface ILoginData {
  username?: string;
  password?: string;
  rememberMe?: boolean;
}

export class LoginData implements ILoginData {
  constructor(
    public email?: string,
    public password?: string,
    public rememberMe?: boolean
  ) { 
    this.email = "admin@gmail.com";
    this.password = "root@admin123";
  }
}

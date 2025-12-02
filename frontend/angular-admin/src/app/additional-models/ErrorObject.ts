export interface IErrorObject {
  errorCode?: number;
  errorStatus?: string;
  errorText?: string;
  stringErrorCode?: string;
}

export class ErrorObject implements IErrorObject {
  constructor(
    public errorCode?: number,
    public errorStatus?: string,
    public errorText?: string,
    public stringErrorCode?: string
  ) { }
}

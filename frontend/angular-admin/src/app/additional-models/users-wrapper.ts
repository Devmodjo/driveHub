import { User } from "../generated/model/user";

export interface UserWrapper {
  user?: User;
  accessToken?: string;
  refreshToken?: string;
  tokenExpiredAt?: string;
}

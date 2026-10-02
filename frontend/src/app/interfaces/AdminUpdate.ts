import { Role } from "../enums/role.enum"

export interface AdminUpdate {
    name: string,
    email:string,
    role : Role,
    /** Facultatif : le mot de passe se change via PATCH /admin/me/password. */
    password?:string,
    residence:string,
    phoneNumber:string
}
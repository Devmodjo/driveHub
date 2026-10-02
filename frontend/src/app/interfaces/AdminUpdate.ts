import { Role } from "../enums/role.enum"

export interface AdminUpdate {
    name: string,
    email:string,
    role : Role,
    password:string,
    residence:string,
    phoneNumber:string
}
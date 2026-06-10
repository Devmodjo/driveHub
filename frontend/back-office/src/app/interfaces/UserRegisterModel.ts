import { Role } from "../enums/role.enum";

interface UserRegisterModel {
    name: string,
    email:string,
    role : Role,
    password:string,
    residence:string,
    phoneNumber:string,
    reason:string
}

export default UserRegisterModel;
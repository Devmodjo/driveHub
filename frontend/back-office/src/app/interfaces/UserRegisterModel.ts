import { Role } from "../enums/role.enum";

interface UserRegisterModel {
    name: string,
    email:string,
    role : Role,
    password:string,
    residence:string,
    phoneNumber:string,
}

export default UserRegisterModel;
import AdminStatus from "../enums/adminstatus.enum";
import { Role } from "../enums/role.enum";

export interface AdminProfile {
    id: string,
    name: string,
    email: string,
    role : Role,
    adminStatus: AdminStatus,
    createdAt: Date
}
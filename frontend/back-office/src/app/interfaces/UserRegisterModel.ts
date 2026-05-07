interface UserRegisterModel {
    id: string,
    email:string,
    role : "REVIEWER" | "SUPER_ADMIN",
    password:string,
    residence:string,
    phoneNumber:string
}

export default UserRegisterModel;
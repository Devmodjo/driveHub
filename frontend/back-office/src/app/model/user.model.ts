import { Role } from "../enums/role.enum";

export class UserModel {

  public  name?: string;
  public  email?: string;
  public  role?: Role;
  public  password?: string;
  public residence?: string;
  public  phoneNumber?: string;

  constructor(
    name?: string,
    email?: string,
    role?: Role,
    password?: string,
    residence?: string,
    phoneNumber?: string
  ) {
    this.name = name;
    this.email = email;
    this.role = role;
    this.password = password;
    this.residence = residence;
    this.phoneNumber = phoneNumber;
  }

  // ─── Getters ───────────────────────────────────────────────────────────────

  getName(): string | undefined {
    return this.name;
  }

  getEmail(): string | undefined {
    return this.email;
  }

  getRole(): Role| undefined {
    return this.role;
  }

  getPassword(): string | undefined {
    return this.password;
  }

  getResidence(): string | undefined {
    return this.residence;
  }

  getPhoneNumber(): string | undefined {
    return this.phoneNumber;
  }

  // ─── Setters ───────────────────────────────────────────────────────────────

  setName(name: string): void {
    this.name = name;
  }

  setEmail(email: string): void {
    this.email = email;
  }

  setRole(role: Role): void {
    this.role = role;
  }

  setPassword(password: string): void {
    this.password = password;
  }

  setResidence(residence: string): void {
    this.residence = residence;
  }

  setPhoneNumber(phoneNumber: string): void {
    this.phoneNumber = phoneNumber;
  }

  // ─── Helpers ───────────────────────────────────────────────────────────────

  isSuperAdmin(): boolean {
    return this.role === 'SUPER_ADMIN';
  }

  isReviewer(): boolean {
    return this.role === 'REVIEWER';
  }

  toString(): string {
    return `UserModel { name: ${this.name}, email: ${this.email}, role: ${this.role}, residence: ${this.residence}, phoneNumber: ${this.phoneNumber} }`;
  }
}

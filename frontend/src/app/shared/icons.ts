import {
  ArrowRight, Bell, BookOpen, Calendar, Car, ChartLine, Check, ChevronDown, CircleCheck, ClipboardList,
  CreditCard, Globe, GraduationCap, LayoutDashboard, LogOut, Mail, MapPin, Menu, Moon, Pencil, Phone, Plus,
  Quote, RefreshCw, School, Search, Settings, Star, Sun, Target, Trash2, UserPlus, Users, Wallet, X,
} from 'lucide-angular';

/**
 * Icônes Lucide utilisées dans l'application (mêmes icônes que lucide-react dans l'ancienne landing).
 * Les importer ici, à un seul endroit, évite de répéter les imports dans chaque composant :
 *
 *   protected readonly icons = ICONS;
 *   <lucide-icon [img]="icons.ArrowRight" [size]="22" />
 *
 * Correspondance des anciens noms : CheckCircle / CheckCircle2 → CircleCheck, LineChart → ChartLine.
 */
export const ICONS = {
  ArrowRight, Bell, BookOpen, Calendar, Car, ChartLine, Check, ChevronDown, CircleCheck, ClipboardList,
  CreditCard, Globe, GraduationCap, LayoutDashboard, LogOut, Mail, MapPin, Menu, Moon, Pencil, Phone, Plus,
  Quote, RefreshCw, School, Search, Settings, Star, Sun, Target, Trash2, UserPlus, Users, Wallet, X,
} as const;

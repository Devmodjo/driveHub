import {
  LucideArrowRight, LucideBell, LucideBookOpen, LucideCalendar, LucideCar, LucideChartLine, LucideCheck,
  LucideChevronDown, LucideCircleCheck, LucideCircleCheckBig, LucideCreditCard, LucideGlobe, LucideGraduationCap,
  LucideLayoutDashboard, LucideLogOut, LucideMail, LucideMapPin, LucideMenu, LucideMoon, LucidePencil, LucidePhone,
  LucidePlus, LucideRefreshCw, LucideSchool, LucideSearch, LucideSettings, LucideSun, LucideTarget, LucideTrash,
  LucideUserPlus, LucideUsers, LucideWallet, LucideX,
  LucideArrowLeft, LucideBan, LucideBuilding2, LucideChevronLeft, LucideChevronRight, LucideCircleX, LucideClock, LucideCopy, LucideEye, LucideEyeOff, LucideHistory, LucideInbox, LucideInfo, LucideKeyRound, LucideLoaderCircle, LucideRotateCcw, LucideSend, LucideShieldCheck, LucideTriangleAlert, LucideUser, LucideUserCheck, LucideUserRound, LucideUserX,
} from '@lucide/angular';

/**
 * Icônes Lucide de l'application.
 *
 * Paquet officiel @lucide/angular, même version que lucide-react dans l'ancienne landing Next.js :
 * les dessins des icônes sont donc strictement identiques.
 *
 * Utilisation dans un composant (importer LucideDynamicIcon) :
 *
 *   protected readonly icons = ICONS;
 *   <svg [lucideIcon]="icons.ArrowRight" [size]="22" [strokeWidth]="3" class="text-white"></svg>
 *
 * L'icône EST le <svg> (pas d'élément intermédiaire), exactement comme avec lucide-react.
 *
 * Anciens noms lucide-react → noms actuels :
 *   CheckCircle → CircleCheckBig, CheckCircle2 → CircleCheck, LineChart → ChartLine, Trash2 → Trash.
 */
export const ICONS = {
  ArrowRight: LucideArrowRight,
  Bell: LucideBell,
  BookOpen: LucideBookOpen,
  Calendar: LucideCalendar,
  Car: LucideCar,
  ChartLine: LucideChartLine,
  Check: LucideCheck,
  ChevronDown: LucideChevronDown,
  CircleCheck: LucideCircleCheck,
  CircleCheckBig: LucideCircleCheckBig,
  CreditCard: LucideCreditCard,
  Globe: LucideGlobe,
  GraduationCap: LucideGraduationCap,
  LayoutDashboard: LucideLayoutDashboard,
  LogOut: LucideLogOut,
  Mail: LucideMail,
  MapPin: LucideMapPin,
  Menu: LucideMenu,
  Moon: LucideMoon,
  Pencil: LucidePencil,
  Phone: LucidePhone,
  Plus: LucidePlus,
  RefreshCw: LucideRefreshCw,
  School: LucideSchool,
  Search: LucideSearch,
  Settings: LucideSettings,
  Sun: LucideSun,
  Target: LucideTarget,
  Trash: LucideTrash,
  UserPlus: LucideUserPlus,
  Users: LucideUsers,
  Wallet: LucideWallet,
  X: LucideX,
  ArrowLeft: LucideArrowLeft,
  Ban: LucideBan,
  Building2: LucideBuilding2,
  ChevronLeft: LucideChevronLeft,
  ChevronRight: LucideChevronRight,
  CircleX: LucideCircleX,
  Clock: LucideClock,
  Copy: LucideCopy,
  Eye: LucideEye,
  EyeOff: LucideEyeOff,
  History: LucideHistory,
  Inbox: LucideInbox,
  Info: LucideInfo,
  KeyRound: LucideKeyRound,
  LoaderCircle: LucideLoaderCircle,
  RotateCcw: LucideRotateCcw,
  Send: LucideSend,
  ShieldCheck: LucideShieldCheck,
  TriangleAlert: LucideTriangleAlert,
  User: LucideUser,
  UserCheck: LucideUserCheck,
  UserRound: LucideUserRound,
  UserX: LucideUserX,
} as const;

import type { Metadata } from "next";
import { Lato, Plus_Jakarta_Sans } from "next/font/google";
import "./globals.css";
import { Navbar } from "@/components/layout/Navbar";
import { Footer } from "@/components/layout/Footer";
import { ThemeProvider } from "@/components/ThemeProvider";

const lato = Lato({
  variable: "--font-lato",
  subsets: ["latin"],
  weight: ["100", "300", "400", "700", "900"],
});

const jakarta = Plus_Jakarta_Sans({
  variable: "--font-jakarta",
  subsets: ["latin"],
});

import { cookies } from 'next/headers';
import { getDictionary, Locale } from '@/i18n/getDictionary';
import { DictionaryProvider } from '@/components/DictionaryProvider';

export const metadata: Metadata = {
  metadataBase: new URL("https://drivehub.cm"),
  title: {
    default: "DriveHub | Le Hub Digital des Auto-Écoles en Afrique",
    template: "%s | DriveHub"
  },
  description: "Simplifiez la gestion de votre auto-école avec DriveHub. Gestion des élèves, plannings, paiements mobiles et suivi des véhicules en une seule plateforme bilingue adaptée au Cameroun.",
  keywords: ["auto-école", "Cameroun", "gestion auto-école", "permis de conduire", "DriveHub", "logiciel auto-école", "Afrique", "Orange Money", "MTN MoMo"],
  authors: [{ name: "DriveHub Team" }],
  creator: "DriveHub",
  publisher: "DriveHub",
  formatDetection: {
    email: false,
    address: false,
    telephone: false,
  },
  openGraph: {
    type: "website",
    locale: "fr_FR",
    url: "https://drivehub.cm",
    siteName: "DriveHub",
    title: "DriveHub | La plateforme de gestion n°1 pour auto-écoles au Cameroun",
    description: "Digitalisez votre auto-école : inscriptions, plannings, et paiements Mobile Money simplifiés.",
    images: [
      {
        url: "/og-image.png",
        width: 1200,
        height: 630,
        alt: "DriveHub Dashboard",
      },
    ],
  },
  twitter: {
    card: "summary_large_image",
    title: "DriveHub | Gestion Digitale pour Auto-Écoles",
    description: "Gérez votre auto-école en toute simplicité au Cameroun.",
    images: ["/og-image.png"],
  },
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      "max-video-preview": -1,
      "max-image-preview": "large",
      "max-snippet": -1,
    },
  },
};

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const cookieStore = await cookies();
  const locale = (cookieStore.get('NEXT_LOCALE')?.value as Locale) || 'fr';
  const dict = getDictionary(locale);

  return (
    <html
      lang={locale}
      className={`${lato.variable} ${jakarta.variable} h-full antialiased scroll-smooth`}
      suppressHydrationWarning
    >
      <body className="min-h-full flex flex-col bg-background text-foreground transition-colors duration-300">
        <ThemeProvider attribute="class" defaultTheme="system" enableSystem disableTransitionOnChange>
          <DictionaryProvider initialDictionary={dict} initialLocale={locale}>
            <Navbar />
            <main className="grow">
              {children}
            </main>
            <Footer />
          </DictionaryProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}

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
  title: "DriveHub | Le Hub Digital des Auto-Écoles en Afrique",
  description: "Simplifiez la gestion de votre auto-école avec DriveHub. Gestion des élèves, plannings, paiements et suivi des véhicules en une seule plateforme.",
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

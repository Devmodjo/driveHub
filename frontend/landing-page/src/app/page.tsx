'use client';

import { Hero } from "@/components/sections/Hero";
import { HowItWorks } from "@/components/sections/HowItWorks";
import { Features } from "@/components/sections/Features";
import { About } from "@/components/sections/About";
import { Pricing } from "@/components/sections/Pricing";
import { Testimonials } from "@/components/sections/Testimonials";
import { FAQ } from "@/components/sections/FAQ";
import { ArrowRight } from "lucide-react";
import { useDictionary } from '@/components/DictionaryProvider';

export default function Home() {
  const { dict } = useDictionary();
  return (
    <div className="flex flex-col">
      <Hero />
      <HowItWorks />
      <Features />
      <About />
      <Pricing />
      <Testimonials />
      <FAQ />
      
      {/* Call to Action Section */}
      <section className="py-32 bg-white dark:bg-black border-t border-black/5 dark:border-white/5 relative overflow-hidden">
         {/* Subtle glow background */}
         <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-2xl h-[300px] bg-[#0070f3]/5 dark:bg-[#0070f3]/10 blur-[130px] rounded-full pointer-events-none" />
         
         <div className="container mx-auto px-6 text-center relative z-10">
            <h2 className="font-display text-4xl md:text-5xl lg:text-6xl font-black text-black dark:text-white mb-8 tracking-tight">
               {dict.CTA.title}
            </h2>
            <p className="text-black/60 dark:text-white/60 text-xl mb-12 max-w-2xl mx-auto font-light leading-relaxed">
               {dict.CTA.description}
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
               <button className="w-full sm:w-auto bg-[#0070f3] text-white hover:bg-[#0070f3]/90 px-8 py-4 rounded-full font-bold text-lg transition-all shadow-lg shadow-[#0070f3]/25 flex items-center justify-center gap-2">
                  {dict.CTA.btn1} <ArrowRight size={20} />
               </button>
               <button className="w-full sm:w-auto bg-black/5 dark:bg-white/5 text-black dark:text-white border border-black/5 dark:border-white/5 hover:bg-black/10 dark:hover:bg-white/10 px-8 py-4 rounded-full font-medium text-lg transition-all backdrop-blur-sm">
                  {dict.CTA.btn2}
               </button>
            </div>
         </div>
      </section>
    </div>
  );
}

'use client';

import React from 'react';
import { motion } from 'framer-motion';
import { Target, CheckCircle2 } from 'lucide-react';
import { useDictionary } from '@/components/DictionaryProvider';
// about components
export const About = () => {
  const { dict } = useDictionary();
  return (
    <section id="about" className="py-32 bg-[#fafafa] dark:bg-[#080808] border-b border-black/5 dark:border-white/5 overflow-hidden">
      <div className="container mx-auto px-6">
        <div className="flex flex-col lg:flex-row items-center gap-16 lg:gap-24 relative">
          
          <motion.div 
            initial={{ opacity: 0, x: -20 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.8 }}
            className="lg:w-1/2"
          >
            <h2 className="text-[10px] font-black text-[#0070f3] uppercase tracking-[0.3em] mb-6 block">{dict.About.badge}</h2>
            <h3 className="font-display text-4xl md:text-5xl lg:text-6xl font-black mb-10 text-black dark:text-white leading-[1.1] tracking-tight">{dict.About.title}</h3>
            
            <div className="space-y-6">
              <p className="text-black/60 dark:text-white/60 text-lg md:text-xl leading-relaxed font-light">
                {dict.About.description1}
              </p>
              <p className="text-black/60 dark:text-white/60 text-lg md:text-xl leading-relaxed font-light">
                {dict.About.description2}
              </p>
            </div>
          </motion.div>

          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 1 }}
            className="lg:w-1/2 w-full relative"
          >
            {/* Professional Premium Box without fake stats */}
            <div className="p-10 md:p-14 rounded-[40px] bg-black dark:bg-[#050505] border border-white/5 text-white shadow-2xl relative overflow-hidden group w-full lg:max-w-lg lg:ml-auto">
               <div className="absolute top-0 right-0 w-64 h-64 bg-[#0070f3]/25 blur-[80px] rounded-full group-hover:scale-125 transition-transform duration-700 pointer-events-none" />
               <div className="absolute -bottom-10 -left-10 w-48 h-48 bg-[#4096ff]/20 blur-[60px] rounded-full pointer-events-none" />
               
               <Target className="text-[#0070f3] mb-8 relative z-10" size={48} strokeWidth={1.5} />
               
               <h4 className="text-3xl lg:text-4xl font-black mb-6 relative z-10 tracking-tight leading-[1.1]">
                  {dict.About.banner_title}
               </h4>
               
               <p className="text-white/60 leading-relaxed font-light text-lg relative z-10 mb-8">
                  {dict.About.banner_desc}
               </p>

               <div className="pt-8 border-t border-white/10 relative z-10 flex items-center justify-between">
                 <div className="flex -space-x-3">
                   <div className="w-10 h-10 rounded-full bg-white/10 border-2 border-black flex items-center justify-center text-[10px] font-bold">PRO</div>
                   <div className="w-10 h-10 rounded-full bg-[#0070f3]/20 border-2 border-black flex items-center justify-center text-[10px] font-bold text-[#0070f3]">B2B</div>
                 </div>
                 <div className="flex gap-1">
                   {[...Array(5)].map((_, i) => (
                     <CheckCircle2 key={i} size={16} className="text-[#0070f3]" />
                   ))}
                 </div>
               </div>
            </div>
          </motion.div>
          
        </div>
      </div>
    </section>
  );
};

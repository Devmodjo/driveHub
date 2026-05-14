'use client';

import React from 'react';
import { motion } from 'framer-motion';
import { Target, CheckCircle2 } from 'lucide-react';
import { useDictionary } from '@/components/DictionaryProvider';

export const About = () => {
  const { dict } = useDictionary();
  return (
    <section id="about" className="py-24 bg-[#fafafa] dark:bg-[#0a0a0a]">
      <div className="container mx-auto px-6">
        <div className="flex flex-col lg:flex-row items-center gap-20">
          <motion.div 
            initial={{ opacity: 0, x: -30 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }}
            className="lg:w-1/2"
          >
            <h2 className="text-sm font-bold text-[#0070f3] uppercase tracking-[0.2em] mb-4">{dict.About.badge}</h2>
            <h3 className="font-display text-4xl md:text-5xl font-black mb-8 text-black dark:text-white leading-[1.1] tracking-tight">{dict.About.title}</h3>
            
            <p className="text-black/60 dark:text-white/60 text-lg mb-8 leading-relaxed font-light">
              {dict.About.description1}
            </p>
            <p className="text-black/60 dark:text-white/60 text-lg mb-8 leading-relaxed font-light">
              {dict.About.description2}
            </p>
          </motion.div>

          <motion.div 
            initial={{ opacity: 0, scale: 0.95 }}
            whileInView={{ opacity: 1, scale: 1 }}
            viewport={{ once: true }}
            className="lg:w-1/2 relative"
          >
            <div className="grid grid-cols-2 gap-4">
               <div className="p-8 rounded-2xl bg-white dark:bg-black border border-black/5 dark:border-white/5 text-center hover:border-[#0070f3]/50 transition-colors">
                  <div className="text-4xl font-black text-[#0070f3] mb-2">{dict.About.stat1_num}</div>
                  <div className="text-xs font-bold uppercase tracking-widest text-black/50 dark:text-white/50">{dict.About.stat1_label}</div>
               </div>
               <div className="p-8 rounded-2xl bg-white dark:bg-black border border-black/5 dark:border-white/5 text-center hover:border-[#0070f3]/50 transition-colors">
                  <div className="text-4xl font-black text-[#0070f3] mb-2">{dict.About.stat2_num}</div>
                  <div className="text-xs font-bold uppercase tracking-widest text-black/50 dark:text-white/50">{dict.About.stat2_label}</div>
               </div>
               <div className="col-span-2 p-10 rounded-3xl bg-black dark:bg-[#111] border border-transparent dark:border-white/5 text-white shadow-xl">
                  <h4 className="text-2xl font-bold mb-4 flex items-center gap-2">
                     <Target className="text-[#0070f3]" /> {dict.About.banner_title}
                  </h4>
                  <p className="text-white/70 leading-relaxed font-light">
                     {dict.About.banner_desc}
                  </p>
               </div>
            </div>
          </motion.div>
        </div>
      </div>
    </section>
  );
};

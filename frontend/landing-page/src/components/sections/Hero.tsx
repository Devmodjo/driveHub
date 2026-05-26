'use client';

import React from 'react';
import { motion } from 'framer-motion';
import { ArrowRight, Search } from 'lucide-react';
import { useDictionary } from '@/components/DictionaryProvider';

export const Hero = () => {
  const { dict } = useDictionary();
  return (
    <section className="relative min-h-[90vh] flex items-center pt-32 pb-20 overflow-hidden">
      {/* Full-width Background Image */}
      <img 
        src="/images/hero-bg-2.jpg" 
        alt="Arrière-plan conducteur" 
        className="absolute inset-0 w-full h-full object-cover"
      />
      
      {/* Blue/Dark Gradient Overlay adapted for typical active imagery */}
      <div className="absolute inset-0 z-0 bg-gradient-to-t from-black via-black/60 to-black/10" />
      
      <div className="container mx-auto px-6 relative z-10">
        <div className="max-w-5xl mx-auto text-center flex flex-col items-center">
          
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, ease: "easeOut" }}
            className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-white/10 border border-white/20 text-[10px] font-black uppercase tracking-[0.2em] text-white/90 mb-12 backdrop-blur-md shadow-2xl"
          >
            Digitalisation Auto-École
          </motion.div>

          <motion.h1
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.8, delay: 0.1, ease: [0.16, 1, 0.3, 1] }}
            className="font-display text-5xl md:text-7xl lg:text-[96px] font-black tracking-tighter text-white mb-8 leading-[0.95] drop-shadow-2xl selection:bg-white selection:text-[#0070f3]"
          >
            {dict.Hero.title1} <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-200 via-white to-gray-300 drop-shadow-lg">
              {dict.Hero.title2}
            </span>
          </motion.h1>
          
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.2, ease: "easeOut" }}
            className="text-lg md:text-2xl text-white/80 mb-12 leading-relaxed max-w-3xl mx-auto font-light"
          >
            {dict.Hero.description}
          </motion.p>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.3, ease: "easeOut" }}
            className="flex flex-col sm:flex-row gap-4 justify-center items-center w-full sm:w-auto"
          >
            <motion.button 
              whileHover={{ scale: 1.05, y: -2 }}
              whileTap={{ scale: 0.95 }}
              className="w-full sm:w-auto bg-[#0070f3] text-white px-10 py-5 rounded-full font-black text-lg transition-all shadow-[0_0_40px_rgba(0,112,243,0.5)] hover:shadow-[0_0_60px_rgba(0,112,243,0.7)] hover:bg-[#0051af] flex items-center justify-center gap-3 backdrop-blur-md"
            >
              {dict.Hero.btn_manage} <ArrowRight size={22} strokeWidth={3} />
            </motion.button>
            <motion.button 
              whileHover={{ scale: 1.02, y: -2, backgroundColor: "rgba(255,255,255,0.15)" }}
              whileTap={{ scale: 0.98 }}
              className="w-full sm:w-auto px-10 py-5 border-2 border-white/40 hover:border-white/60 text-white rounded-full font-bold text-lg transition-all flex items-center justify-center gap-3 backdrop-blur-md"
            >
              {dict.Hero.btn_find} <Search size={22} strokeWidth={2.5} />
            </motion.button>
          </motion.div>
        </div>
      </div>
    </section>
  );
};

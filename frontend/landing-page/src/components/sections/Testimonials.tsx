'use client';

import React from 'react';
import { useDictionary } from '@/components/DictionaryProvider';
import { Quote, Star } from 'lucide-react';
import { motion } from 'framer-motion';

export const Testimonials = () => {
  const { dict } = useDictionary();

  const testimonials = [
    {
      name: dict.Testimonials.t1_name,
      role: dict.Testimonials.t1_role,
      quote: dict.Testimonials.t1_quote,
    },
    {
      name: dict.Testimonials.t2_name,
      role: dict.Testimonials.t2_role,
      quote: dict.Testimonials.t2_quote,
    },
    {
      name: dict.Testimonials.t3_name,
      role: dict.Testimonials.t3_role,
      quote: dict.Testimonials.t3_quote,
    },
  ];

  return (
    <section id="testimonials" className="py-24 bg-white dark:bg-black relative overflow-hidden">
      {/* Background Decor */}
      <div className="absolute top-0 right-0 -translate-y-1/2 translate-x-1/2 w-[500px] h-[500px] bg-[#0070f3]/5 rounded-full blur-[120px] pointer-events-none" />
      
      <div className="container mx-auto px-6 relative z-10">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <motion.span 
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="inline-block px-4 py-1.5 mb-6 text-sm font-bold tracking-wider text-[#0070f3] uppercase bg-[#0070f3]/10 rounded-full"
          >
            {dict.Testimonials.badge}
          </motion.span>
          <motion.h2 
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            className="text-4xl md:text-5xl font-black text-black dark:text-white mb-6 tracking-tight"
          >
            {dict.Testimonials.title}
          </motion.h2>
          <motion.p 
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            className="text-xl text-black/60 dark:text-white/60 font-light"
          >
            {dict.Testimonials.description}
          </motion.p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {testimonials.map((t, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              className="group p-8 bg-black/[0.02] dark:bg-white/[0.02] border border-black/5 dark:border-white/5 rounded-3xl hover:bg-white dark:hover:bg-white/[0.05] hover:shadow-2xl hover:shadow-black/5 transition-all duration-300 relative"
            >
              <div className="absolute -top-4 left-8 w-10 h-10 bg-[#0070f3] rounded-xl flex items-center justify-center text-white shadow-lg shadow-[#0070f3]/20">
                <Quote size={20} fill="currentColor" />
              </div>
              
              <div className="flex gap-1 mb-6 mt-2">
                {[...Array(5)].map((_, i) => (
                  <Star key={i} size={16} className="fill-[#0070f3] text-[#0070f3]" />
                ))}
              </div>

              <p className="text-lg text-black/80 dark:text-white/80 mb-8 italic font-medium leading-relaxed">
                "{t.quote}"
              </p>

              <div className="flex items-center gap-4 border-t border-black/5 dark:border-white/10 pt-6">
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-[#0070f3] to-blue-400 flex items-center justify-center text-white font-bold text-xl">
                  {t.name.charAt(0)}
                </div>
                <div>
                  <h4 className="font-bold text-black dark:text-white">{t.name}</h4>
                  <p className="text-sm text-black/50 dark:text-white/50">{t.role}</p>
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};

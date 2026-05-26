'use client';

import React from 'react';
import { motion } from 'framer-motion';
import { 
  Users, 
  Calendar, 
  CreditCard, 
  BookOpen, 
  Bell,
  LineChart
} from 'lucide-react';
import { useDictionary } from '@/components/DictionaryProvider';

export const Features = () => {
  const { dict } = useDictionary();
  
  const features = [
    { title: dict.Features.f1_title, description: dict.Features.f1_desc, icon: Users },
    { title: dict.Features.f2_title, description: dict.Features.f2_desc, icon: Calendar },
    { title: dict.Features.f3_title, description: dict.Features.f3_desc, icon: CreditCard },
    { title: dict.Features.f4_title, description: dict.Features.f4_desc, icon: BookOpen },
    { title: dict.Features.f5_title, description: dict.Features.f5_desc, icon: Bell },
    { title: dict.Features.f6_title, description: dict.Features.f6_desc, icon: LineChart },
  ];

  return (
    <section id="features" className="py-32 bg-white dark:bg-black border-y border-black/5 dark:border-white/5 relative overflow-hidden">
      <div className="container mx-auto px-6 relative z-10">
        <div className="max-w-3xl mb-24">
          <motion.h2 
            initial={{ opacity: 0, x: -10 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }}
            className="text-[10px] font-black text-[#0070f3] uppercase tracking-[0.3em] mb-6 block"
          >
            {dict.Features.badge}
          </motion.h2>
          <motion.h3 
            initial={{ opacity: 0, y: 10 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="font-display text-4xl md:text-6xl font-black text-black dark:text-white mb-8 tracking-tight leading-[1.1]"
          >
            {dict.Features.title}
          </motion.h3>
          <motion.p 
            initial={{ opacity: 0, y: 10 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-xl md:text-2xl text-black/50 dark:text-white/50 font-light leading-relaxed"
          >
            {dict.Features.description}
          </motion.p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10">
          {features.map((feature, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.6, delay: index * 0.05 }}
              whileHover={{ y: -8, transition: { duration: 0.3 } }}
              className="group p-1 bg-gradient-to-b from-black/[0.03] to-transparent dark:from-white/[0.03] dark:to-transparent rounded-[32px] border border-black/5 dark:border-white/5 transition-colors hover:border-[#0070f3]/30"
            >
              <div className="bg-white dark:bg-[#050505] p-10 rounded-[28px] h-full flex flex-col items-start transition-colors group-hover:bg-[#f8faff] dark:group-hover:bg-[#000814]">
                <div className="w-14 h-14 rounded-2xl bg-black/5 dark:bg-white/5 text-black dark:text-white flex items-center justify-center mb-10 transition-all duration-300 group-hover:bg-[#0070f3] group-hover:text-white group-hover:shadow-[0_10px_20px_rgba(0,112,243,0.2)]">
                  <feature.icon size={28} strokeWidth={1.5} />
                </div>

                <h4 className="text-2xl font-bold text-black dark:text-white mb-5 tracking-tight">{feature.title}</h4>
                <p className="text-black/50 dark:text-white/50 leading-relaxed font-light text-base lg:text-lg">
                  {feature.description}
                </p>
              </div>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};

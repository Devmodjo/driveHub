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
      avatar: "https://images.unsplash.com/photo-1531123897727-8f129e1bfa8ea?ixlib=rb-4.0.3&auto=format&fit=crop&w=150&q=80"
    },
    {
      name: dict.Testimonials.t2_name,
      role: dict.Testimonials.t2_role,
      quote: dict.Testimonials.t2_quote,
      avatar: "https://images.unsplash.com/photo-1589156280159-27698a70f29e?ixlib=rb-4.0.3&auto=format&fit=crop&w=150&q=80"
    },
    {
      name: dict.Testimonials.t3_name,
      role: dict.Testimonials.t3_role,
      quote: dict.Testimonials.t3_quote,
      avatar: "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?ixlib=rb-4.0.3&auto=format&fit=crop&w=150&q=80"
    },
  ];

  return (
    <section id="testimonials" className="py-32 bg-[#fafafa] dark:bg-[#0a0a0a] relative overflow-hidden text-center">
      <div className="container mx-auto px-6 relative z-10">
        <div className="max-w-3xl mx-auto mb-20 lg:mb-24">
          <motion.span 
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="inline-block px-4 py-1.5 mb-6 text-[10px] font-black tracking-[0.2em] text-[#0070f3] uppercase bg-[#0070f3]/10 rounded-full"
          >
            {dict.Testimonials.badge}
          </motion.span>
          <motion.h2 
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            className="text-4xl md:text-5xl font-black text-black dark:text-white mb-6 tracking-tight leading-[1.1]"
          >
            {dict.Testimonials.title}
          </motion.h2>
          <motion.p 
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            className="text-xl text-black/50 dark:text-white/50 font-light"
          >
            {dict.Testimonials.description}
          </motion.p>
        </div>

        {/* Carousel for mobile: horizontal scrolling layout that snaps. Grid on lg screens. */}
        <div className="flex overflow-x-auto lg:grid lg:grid-cols-3 gap-6 lg:gap-8 gap-y-16 lg:gap-y-8 snap-x snap-mandatory pt-12 pb-10 px-4 -mx-4 lg:mx-0 lg:px-0 hide-scroll-bar">
          {testimonials.map((t, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              viewport={{ once: true }}
              className="group flex-shrink-0 w-[85vw] sm:w-[400px] lg:w-auto snap-center mt-8 lg:mt-12 bg-white dark:bg-[#050505] p-10 rounded-[40px] border border-black/5 dark:border-white/5 hover:border-[#0070f3]/20 shadow-xl shadow-black-[0.02] transition-colors relative flex flex-col items-center"
            >
              {/* Overlapping Top-Centered Avatar */}
              <div className="absolute -top-12 left-1/2 -translate-x-1/2 w-24 h-24 rounded-full border-[6px] border-[#fafafa] dark:border-[#0a0a0a] shadow-lg overflow-hidden bg-white dark:bg-black group-hover:scale-105 transition-transform duration-300">
                <img 
                  src={t.avatar} 
                  alt={t.name}
                  className="w-full h-full object-cover"
                />
              </div>

              {/* Minimalist Quote Mark */}
              <div className="text-[#0070f3]/20 mt-4 mb-6">
                <Quote size={32} fill="currentColor" />
              </div>
              
              <div className="flex gap-1 mb-6">
                {[...Array(5)].map((_, i) => (
                  <Star key={i} size={16} className="fill-[#0070f3] text-[#0070f3]" />
                ))}
              </div>

              <p className="text-lg md:text-xl text-black/80 dark:text-white/80 mb-8 italic font-light leading-relaxed flex-grow">
                "{t.quote}"
              </p>

              <div className="border-t border-black/5 dark:border-white/5 pt-6 w-full mt-auto">
                <h4 className="font-bold text-black dark:text-white text-lg">{t.name}</h4>
                <p className="text-xs font-bold uppercase tracking-widest text-[#0070f3] mt-2">{t.role}</p>
              </div>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};

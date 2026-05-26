'use client';

import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { ChevronDown } from 'lucide-react';
import { useDictionary } from '@/components/DictionaryProvider';

export const FAQ = () => {
  const { dict } = useDictionary();
  const [activeIndex, setActiveIndex] = useState<number | null>(null);

  const faqs = [
    {
      question: dict.FAQ.q1,
      answer: dict.FAQ.a1,
    },
    {
      question: dict.FAQ.q2,
      answer: dict.FAQ.a2,
    },
    {
      question: dict.FAQ.q3,
      answer: dict.FAQ.a3,
    },
    {
      question: dict.FAQ.q4,
      answer: dict.FAQ.a4,
    },
  ];

  return (
    <section id="faq" className="py-24 bg-white dark:bg-black">
      <div className="container mx-auto px-6">
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          className="text-center max-w-3xl mx-auto mb-16"
        >
          <h2 className="text-[10px] font-black text-[#0070f3] uppercase tracking-[0.3em] mb-4">{dict.FAQ.badge}</h2>
          <h3 className="font-display text-4xl md:text-5xl lg:text-6xl font-black text-black dark:text-white mb-6 tracking-tight leading-[1.1]">{dict.FAQ.title}</h3>
          <p className="text-xl md:text-2xl text-black/60 dark:text-white/60 font-light leading-relaxed">
            {dict.FAQ.description}
          </p>
        </motion.div>

        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.2 }}
          className="max-w-3xl mx-auto space-y-4"
        >
          {faqs.map((faq, index) => (
            <div
              key={index}
              className="bg-black/5 dark:bg-white/5 border border-black/5 dark:border-white/5 rounded-2xl overflow-hidden transition-all duration-300 hover:border-[#0070f3]/30"
            >
              <button
                className="w-full px-6 py-6 flex items-center justify-between text-left group"
                onClick={() => setActiveIndex(activeIndex === index ? null : index)}
              >
                <span className="font-display font-black text-lg text-black dark:text-white group-hover:text-[#0070f3] transition-colors">{faq.question}</span>
                <div className={`w-8 h-8 rounded-full flex items-center justify-center bg-black/5 dark:bg-white/5 shrink-0 transition-transform duration-300 ${activeIndex === index ? 'rotate-180 bg-[#0070f3] text-white' : 'text-black/50 dark:text-white/50'}`}>
                  <ChevronDown size={18} />
                </div>
              </button>
              
              <AnimatePresence>
                {activeIndex === index && (
                  <motion.div
                    initial={{ height: 0, opacity: 0 }}
                    animate={{ height: 'auto', opacity: 1 }}
                    exit={{ height: 0, opacity: 0 }}
                  >
                    <div className="px-6 pb-6 pt-2 text-black/60 dark:text-white/60 text-base leading-relaxed font-light">
                      {faq.answer}
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          ))}
        </motion.div>
      </div>
    </section>
  );
};

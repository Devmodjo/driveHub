'use client';

import React from 'react';

interface LogoIconProps {
  className?: string;
}

export const LogoIcon = ({ className = '' }: LogoIconProps) => {
  const blueColor = "#0070f3";
  
  return (
    <div className={`relative w-12 h-12 rounded-xl overflow-hidden shadow-lg shadow-[#0070f3]/20 border border-black/5 dark:border-white/10 transition-colors duration-300 bg-white dark:bg-black ${className}`}>
      <svg
        viewBox="0 0 100 100"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className="w-full h-full p-2"
      >
        {/* 'D' Part */}
        <path
          d="M20 30 H50 C65 30 75 40 75 50 C75 60 65 70 50 70 H20 L25 50 Z"
          fill={blueColor}
        />
        <path
          d="M20 30 L25 50 L20 70"
          stroke={blueColor}
          strokeWidth="8"
          strokeLinecap="round"
        />
        
        {/* Connection point */}
        <circle cx="58" cy="50" r="4" className="fill-black dark:fill-white" stroke={blueColor} strokeWidth="2" />
        
        {/* 'H' Part */}
        <path
          d="M62 30 V70 M85 30 V70 M62 50 H85"
          className="stroke-black dark:stroke-white"
          strokeWidth="8"
          strokeLinecap="round"
        />
      </svg>
    </div>
  );
};

import React from 'react'
import { EnergyRing } from './EnergyRing'
import { DEMO_DAY } from '../config'

export const HeroPhoneScreen: React.FC = () => {
  return (
    <div className="w-full h-full bg-[#141816] text-[#f4f6f4] p-2 sm:p-3 flex flex-col justify-between overflow-hidden select-none text-left">
      {/* App bar with User greeting & Sproutling cameo */}
      <div className="flex items-center justify-between pb-1.5 sm:pb-2 border-b border-white/5">
        <div>
          <span className="text-[10px] sm:text-[11px] text-[#858f89]">Hey, Alex 👋</span>
          <h4 className="text-[11px] sm:text-xs font-medium text-[#f4f6f4] leading-tight">Lose weight &bull; Day 1</h4>
        </div>
        <div className="flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-[#1e2420] border border-white/5">
          <img
            src="/assets/sproutling/ic_sproutling_active.svg"
            width="14"
            height="14"
            alt="Sproutling"
          />
          <span className="text-[10px] sm:text-[11px] font-medium text-[#38c77b]">On track</span>
        </div>
      </div>

      {/* Hero Energy Card matching real app Home screen */}
      <div className="my-1 sm:my-1.5 p-2 sm:p-2.5 rounded-xl sm:rounded-2xl bg-[#191d1b] border border-white/5 shadow-xs">
        <div className="flex items-center justify-between pb-1 mb-1 border-b border-white/5">
          <div className="flex items-center gap-1.5">
            <span className="w-1.5 h-1.5 rounded-full bg-[#38c77b]" />
            <span className="text-[9px] sm:text-[10px] font-bold tracking-wider uppercase text-[#858f89]">
              Daily Energy Arc
            </span>
          </div>
          <span className="text-[9px] sm:text-[10px] font-mono font-medium text-[#38c77b] bg-[#14261d] px-1.5 py-0.5 rounded border border-[#38c77b]/30">
            Target: {DEMO_DAY.calorieTarget} kcal
          </span>
        </div>

        {/* Energy Ring + Centered Remaining + 3 Macro bars */}
        <EnergyRing size={112} strokeWidth={7} />
      </div>

      {/* Logged meals list: Dal tadka (420) + Chicken rice (540) + Paneer bhurji (380) + Curd fruit (283) = 1,623 kcal */}
      <div className="space-y-1 pt-1 border-t border-white/5">
        <div className="flex items-center justify-between text-[10px] sm:text-[11px] text-[#858f89]">
          <span className="font-medium">Logged meals</span>
          <span className="font-mono tabular-nums text-[#38c77b] font-semibold">{DEMO_DAY.calorieConsumed} kcal</span>
        </div>

        <div className="space-y-0.5 sm:space-y-1 max-h-[120px] overflow-hidden">
          {DEMO_DAY.meals.map((meal) => (
            <div
              key={meal.id}
              className="flex items-center justify-between px-2 py-0.5 sm:py-1 rounded-md bg-[#191d1b] border border-white/5 text-[10px] sm:text-[11px]"
            >
              <span className="text-[#f4f6f4] truncate max-w-[130px] sm:max-w-[145px]">{meal.name}</span>
              <span className="font-mono tabular-nums text-[#b2bab5] font-medium">{meal.calories} kcal</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

import React from "react";
import { Link } from "react-router-dom";
import Icon from "../components/ui/Icon";

const FEATURES = [
  ["shield", "Real coverage", "Home and vehicle policies with readable terms."],
  ["bolt", "Fast claims", "Upload evidence, submit claims, and track every status change."],
  ["lock", "Verified identity", "KYC checks protect policy purchase, renewal, and settlement."],
];

const POLICIES = [
  ["Vehicle Protect", "Roadside help, accident cover, repair partner support.", "from ₹84/mo"],
  ["Home Secure", "Fire, theft, water damage, valuables, rental support.", "from ₹129/mo"],
];

const REVIEWS = [
  ["Aarav M.", "My claim moved from upload to approval in two days. No confusing forms."],
  ["Neha S.", "The dashboard made my car and home policies feel easy to manage."],
  ["Kiran P.", "Renewal reminders and document previews saved a lot of back and forth."],
];

const MOCK_PHOTOS = [
  ["Apartment protection", "/images/house-apartment.jpg"],
  ["Home insurance", "/images/house-house.jpg"],
  ["Window damage cover", "/images/house-windows.jpg"],
  ["Apartment Coverage", "/images/home-apartment-2.jpg"],
  ["Bike insurance", "/images/vehicle-bike-drive.jpg"],
  ["Two wheeler cover", "/images/vehicle-bike-simple.jpg"],
  ["Car protection", "/images/vehicle-blue-car.jpg"],
  ["Scooter cover", "/images/vehicle-red-scooter.jpg"],
  ["Private car cover", "/images/vehicle-white-car.jpg"],
  ["Commercial van cover", "/images/vehicle-white-van.jpg"],
];

export default function Landing() {
  return (
    <div>
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 -z-10 bg-gradient-to-br from-bg via-bg to-[#edf4ff]" />
        <div className="hidden lg:block absolute inset-y-0 right-0 w-1/2 overflow-hidden bg-surface">
          <div className="flex h-full w-max landing-photo-step-scroll">
            {[...MOCK_PHOTOS, ...MOCK_PHOTOS].map(([label, src], index) => (
              <div key={`${label}-${index}`} className="relative h-full w-[50vw] shrink-0 overflow-hidden">
                <img src={src} alt={label} className="h-full w-full object-cover" />
                <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/65 to-transparent p-8 text-white">
                  <p className="font-heading text-3xl font-semibold">{label}</p>
                  <p className="text-sm text-white/80">SmartSure home and vehicle coverage</p>
                </div>
              </div>
            ))}
          </div>
        </div>
        <div className="mx-auto max-w-[1760px] px-5 sm:px-10 lg:px-20 py-16 sm:py-20 grid lg:grid-cols-2 gap-10 xl:gap-16 items-center min-h-[640px]">
          <div className="relative z-10">
            <span className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-surface border border-border text-xs font-semibold text-text-muted">
              <span className="w-1.5 h-1.5 rounded-full bg-success" />
              Insurance, simplified.
            </span>
            <h1 className="mt-5 font-heading text-5xl sm:text-6xl xl:text-7xl font-semibold leading-[1.03]">
              Coverage you understand,
              <br />
              <span className="text-brand">Confidence you can feel.</span>
            </h1>
            <p className="mt-5 text-lg text-text-muted max-w-2xl">
              SmartSure brings home and vehicle insurance into one clear dashboard. Compare plans, buy policies,
              complete KYC, file claims, and watch settlement progress without clutter.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link
                to="/register"
                className="inline-flex items-center gap-2 px-5 py-3 rounded-md bg-brand text-white font-semibold hover:bg-brand-hover transition-colors"
              >
                Create an account
                <Icon name="arrow-right" />
              </Link>
              <Link
                to="/login"
                className="inline-flex items-center gap-2 px-5 py-3 rounded-md border border-border bg-surface font-semibold hover:bg-surface-2 transition-colors"
              >
                Sign in
              </Link>
            </div>
          </div>

          <div className="relative min-h-[420px] overflow-hidden rounded-lg border border-border bg-surface shadow-elevated lg:hidden">
            <div className="absolute inset-0 flex h-full w-max landing-photo-step-scroll-mobile">
              {[...MOCK_PHOTOS, ...MOCK_PHOTOS].map(([label, src], index) => (
                <div
                  key={`${label}-${index}`}
                  className="relative h-full w-[calc(100vw-40px)] sm:w-[calc(100vw-80px)] shrink-0 overflow-hidden"
                >
                  <img src={src} alt={label} className="h-full w-full object-cover" />
                  <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/65 to-transparent p-5 text-white">
                    <p className="font-heading text-2xl font-semibold">{label}</p>
                    <p className="text-sm text-white/80">SmartSure home and vehicle coverage</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* <section className="border-y border-border bg-surface">
        <div className="mx-auto max-w-[1760px] px-5 sm:px-10 lg:px-20">
          <div className="grid divide-y divide-border sm:grid-cols-3 sm:divide-x sm:divide-y-0">
            {[
              ["₹42Cr+", "Claims paid"],
              ["84k+", "Happy users"],
              ["4.8/5", "Service rating"],
            ].map(([value, label]) => (
              <div key={label} className="py-8 sm:px-10 first:sm:pl-0 last:sm:pr-0">
                <p className="font-heading text-4xl sm:text-5xl font-semibold">{value}</p>
                <p className="mt-2 text-xs uppercase tracking-widest text-text-subtle">{label}</p>
              </div>
            ))}
          </div>
        </div>
      </section> */}

      <section className="border-y border-border bg-surface">
        <div className="mx-auto max-w-[1760px] px-5 sm:px-10 lg:px-20 py-[72px] sm:py-20">
          <p className="text-xs font-semibold tracking-widest uppercase text-brand">Built for clarity</p>
          <h2 className="font-heading text-3xl sm:text-4xl font-semibold mt-2">Everything needed. Nothing noisy.</h2>
          <div className="mt-10 grid md:grid-cols-3 gap-4">
            {FEATURES.map(([icon, title, body]) => (
              <div key={title} className="rounded-lg border border-border bg-bg p-6 hover:border-border-strong transition-colors">
                <div className="grid place-items-center w-10 h-10 rounded-md bg-brand-soft text-brand">
                  <Icon name={icon} />
                </div>
                <h3 className="font-heading text-xl font-semibold mt-4">{title}</h3>
                <p className="text-sm text-text-muted mt-1">{body}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="bg-bg">
        <div className="mx-auto max-w-[1760px] px-5 sm:px-10 lg:px-20 py-[72px] sm:py-20">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <p className="text-xs font-semibold tracking-widest uppercase text-brand">Policy types</p>
              <h2 className="font-heading text-4xl font-semibold mt-2">Choose cover that fits real life.</h2>
            </div>
            <Link to="/register" className="inline-flex items-center gap-2 text-sm font-semibold text-brand">
              Start comparing <Icon name="arrow-right" />
            </Link>
          </div>
          <div className="mt-10 grid gap-4 md:grid-cols-2">
            {POLICIES.map(([title, body, price], index) => (
              <div key={title} className={`rounded-lg border border-border p-6 ${index % 2 ? "bg-surface" : "bg-[#eef6f3]"}`}>
                <p className="text-sm font-semibold text-brand">{price}</p>
                <h3 className="mt-3 font-heading text-2xl font-semibold">{title}</h3>
                <p className="mt-3 text-sm leading-6 text-text-muted">{body}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="relative overflow-hidden bg-[#0b1220] text-white">
        <img
          src="/images/house-house.jpg"
          alt=""
          className="absolute inset-0 h-full w-full object-cover opacity-35"
        />
        <div className="absolute inset-0 bg-[#0b1220]/70" />
        <div className="relative mx-auto max-w-[1760px] px-5 sm:px-10 lg:px-20 py-20">
          <div className="grid gap-8 md:grid-cols-4">
            {[
              ["₹42Cr+", "claims paid"],
              ["18 min", "average claim draft time"],
              ["84k+", "happy users"],
              ["96%", "renew online"],
            ].map(([value, label]) => (
              <div key={label} className="rounded-lg border border-white/15 bg-white/10 p-6 backdrop-blur">
                <p className="font-heading text-5xl font-semibold">{value}</p>
                <p className="mt-2 text-sm uppercase tracking-widest text-white/75">{label}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="bg-surface">
        <div className="mx-auto max-w-[1760px] px-5 sm:px-10 lg:px-20 py-[72px] sm:py-20">
          <p className="text-xs font-semibold tracking-widest uppercase text-brand">Customer stories</p>
          <h2 className="font-heading text-4xl font-semibold mt-2">Reviews from policyholders.</h2>
          <div className="mt-12 space-y-8">
            {REVIEWS.map(([name, quote], index) => (
              <div
                key={name}
                className={`max-w-3xl rounded-lg border border-border bg-bg p-7 shadow-soft ${
                  index % 2 ? "ml-auto" : ""
                }`}
              >
                <p className="font-heading text-2xl leading-8">"{quote}"</p>
                <p className="mt-4 text-sm font-semibold text-brand">{name}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="bg-bg">
        <div className="mx-auto max-w-[1760px] px-5 sm:px-10 lg:px-20 py-[72px] sm:py-20 grid gap-8 lg:grid-cols-2">
          <div>
            <p className="text-xs font-semibold tracking-widest uppercase text-brand">How claims work</p>
            <h2 className="font-heading text-4xl font-semibold mt-2">From incident to payout, tracked clearly.</h2>
          </div>
          <div className="grid gap-4">
            {["Create claim", "Upload proof", "Review updates", "Settlement paid"].map((step, index) => (
              <div key={step} className="flex gap-4 rounded-lg border border-border bg-surface p-5">
                <div className="grid h-10 w-10 shrink-0 place-items-center rounded-md bg-brand text-white font-semibold">
                  {index + 1}
                </div>
                <div>
                  <h3 className="font-heading text-xl font-semibold">{step}</h3>
                  <p className="mt-1 text-sm text-text-muted">
                    Simple status notes, document checks, and customer-visible timelines keep every step understandable.
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}

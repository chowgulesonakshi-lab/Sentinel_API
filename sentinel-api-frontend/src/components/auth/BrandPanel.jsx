import sentinelLogo from "../../assets/sentinel-logo.png";

function BrandPanel() {
    return (
        <div className="hidden md:flex flex-col justify-center w-1/2 bg-slate-900 text-white px-16">

            <div className="max-w-2xl">

                <div className="flex items-center gap-8">

                    <img
                        src={sentinelLogo}
                        alt="Sentinel"
                        className="w-36 h-36 object-contain flex-shrink-0"
                    />

                    <div>
                        <h1 className="text-6xl font-bold tracking-tight">
                            Sentinel API
                        </h1>

                        <p className="text-xl text-slate-300 leading-relaxed mt-5">
                            Keep your APIs healthy.
                            <br />
                            Monitor performance in real time.
                        </p>
                    </div>

                </div>

                <div className="ml-[176px] mt-8 text-sm text-slate-400">
                    Real-time API monitoring platform
                </div>

            </div>

        </div>
    );
}

export default BrandPanel;
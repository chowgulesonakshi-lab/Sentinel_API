import { useLocation } from "react-router-dom";
import ThemeToggle from "./ThemeToggle";

function Navbar() {

    const location = useLocation();

    const pageTitles = {
        "/dashboard": "Dashboard",
        "/projects": "Projects",
        "/services": "Services",
        "/endpoints": "Endpoints",
        "/metrics": "Metrics",
        "/alerts": "Alerts"
    };

    return (
        <header
            className="
                bg-white dark:bg-slate-900
                border-b border-slate-200 dark:border-slate-800
                px-8 py-4
                flex items-center justify-between
                transition-colors
            "
        >

            {/* Left */}

            <div>

                <h2 className="
                    text-2xl
                    font-bold
                    text-slate-900 dark:text-white
                ">
                    {pageTitles[location.pathname] || "Sentinel"}
                </h2>

                <p className="
                    text-sm
                    text-slate-500 dark:text-slate-400
                    mt-1
                ">
                    Monitor • Analyze • Optimize
                </p>

            </div>


            {/* Right */}

            <div className="flex items-center gap-5">

                <div className="
                    hidden md:flex
                    items-center gap-2
                    px-3 py-2
                    rounded-full
                    bg-green-100 dark:bg-green-500/20
                    text-green-700 dark:text-green-400
                    text-sm font-medium
                ">

                    <span className="w-2 h-2 rounded-full bg-green-500"></span>

                    Operational

                </div>


                <div className="
                    hidden lg:block
                    text-sm
                    text-slate-500 dark:text-slate-400
                ">
                    Last Scan: {new Date().toLocaleTimeString([], {
                        hour: "2-digit",
                        minute: "2-digit"
                    })}
                </div>


                <ThemeToggle />

            </div>

        </header>
    );
}

export default Navbar;
import { NavLink } from "react-router-dom";

function Sidebar() {

    const linkClass = ({ isActive }) =>
        `flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 ${
            isActive
                ? "bg-blue-600 text-white shadow-lg"
                : "text-slate-300 hover:bg-slate-800 hover:text-white"
        }`;

    return (
        <aside className="w-64 min-h-screen bg-slate-900 border-r border-slate-800 flex flex-col">

            {/* Logo */}
            <div className="px-6 py-8 border-b border-slate-800">

                <h1 className="text-3xl font-bold text-white">
                    🛡️ Sentinel
                </h1>

                <p className="text-sm text-slate-400 mt-1">
                    API Monitoring Platform
                </p>

            </div>

            {/* Navigation */}
            <nav className="flex-1 px-4 py-6 space-y-2">

                <NavLink to="/dashboard" className={linkClass}>
                    📊 Dashboard
                </NavLink>

                <NavLink to="/projects" className={linkClass}>
                    📁 Projects
                </NavLink>

                <NavLink to="/services" className={linkClass}>
                    📦 Services
                </NavLink>

                <NavLink to="/endpoints" className={linkClass}>
                    🌐 Endpoints
                </NavLink>

                <NavLink to="/metrics" className={linkClass}>
                    📈 Metrics
                </NavLink>

                <NavLink to="/alerts" className={linkClass}>
                    🚨 Alerts
                </NavLink>

            </nav>

            {/* Footer */}
            <div className="border-t border-slate-800 p-5">

                <button
                    className="
                        w-full
                        bg-red-600 hover:bg-red-700
                        rounded-lg
                        py-2
                        text-white
                        transition-colors
                    "
                >
                    Logout
                </button>

            </div>

        </aside>
    );
}

export default Sidebar;
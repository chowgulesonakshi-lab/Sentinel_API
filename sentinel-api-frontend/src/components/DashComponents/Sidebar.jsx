import { NavLink } from "react-router-dom";
import {LayoutDashboard, FolderKanban, Layers3, Globe, ChartNoAxesCombined,
    Siren, GitBranch} from "lucide-react";
import sentinelLogo from "../../assets/sentinel-logo.png";
import { useNavigate } from "react-router-dom";
import { LogOut } from "lucide-react";

function Sidebar() {
    const navigate = useNavigate();
    const linkClass = ({ isActive }) =>
        `flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 ${
            isActive
                ? "bg-blue-600 text-white shadow-lg"
                : "text-slate-300 hover:bg-slate-800 hover:text-white"
        }`;
    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        navigate("/");
    };

    return (
        <aside className="w-64 min-h-screen bg-slate-900 border-r border-slate-800 flex flex-col">
            <div className="px-6 py-8 border-b border-slate-800">
                <div className="flex items-center gap-3">
                    <img
                        src={sentinelLogo}
                        alt="Sentinel"
                        className="w-10 h-10 object-contain"
                    />
                    <h1 className="text-2xl font-bold text-white">
                        Sentinel
                    </h1>
                </div>

                <p className="text-sm text-slate-400 mt-2">
                    API Monitoring Platform
                </p>
            </div>

            <nav className="flex-1 px-4 py-6 space-y-2">
                <NavLink to="/dashboard" className={linkClass}>
                    <LayoutDashboard size={19} />
                    <span>Dashboard</span>
                </NavLink>

                <NavLink to="/projects" className={linkClass}>
                    <FolderKanban size={19} />
                    <span>Projects</span>
                </NavLink>

                <NavLink to="/services" className={linkClass}>
                    <Layers3 size={19} />
                    <span>Services</span>
                </NavLink>

                <NavLink to="/endpoints" className={linkClass}>
                    <Globe size={19} />
                    <span>Endpoints</span>
                </NavLink>

                <NavLink to="/metrics" className={linkClass}>
                    <ChartNoAxesCombined size={19} />
                    <span>Metrics</span>
                </NavLink>

                <NavLink to="/alerts" className={linkClass}>
                    <Siren size={19} />
                    <span>Alerts</span>
                </NavLink>

                <NavLink to="/dependencies" className={linkClass}>
                    <GitBranch size={19} />
                    <span>Dependencies</span>
                </NavLink>

            </nav>

            <div className="border-t border-slate-800 p-5">
                <button
                    onClick={handleLogout}
                    className="
                        w-full
                        bg-red-600 hover:bg-red-700
                        rounded-lg
                        py-2
                        text-white
                        transition-colors
                        flex items-center justify-center gap-2
                    "
                >
                    <LogOut size={17} strokeWidth={1.8} />
                    Logout
                </button>
            </div>
        </aside>
    );
}

export default Sidebar;

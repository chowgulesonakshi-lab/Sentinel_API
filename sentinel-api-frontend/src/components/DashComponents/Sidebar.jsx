import { NavLink } from "react-router-dom";

function Sidebar() {

    const linkClass = ({ isActive }) =>
        `block px-4 py-2 rounded-lg transition ${
            isActive
                ? "bg-blue-600 text-white"
                : "hover:bg-slate-800 text-slate-300"
        }`;

    return (
        <div className="w-64 min-h-screen bg-slate-900 text-white p-6">

            <h1 className="text-2xl font-bold mb-10">
                Sentinel
            </h1>

            <nav className="space-y-2">

                <NavLink to="/dashboard" className={linkClass}>
                    Dashboard
                </NavLink>

                <NavLink to="/projects" className={linkClass}>
                    Projects
                </NavLink>

                <NavLink to="/services" className={linkClass}>
                    Services
                </NavLink>

                <NavLink to="/endpoints" className={linkClass}>
                    Endpoints
                </NavLink>

                <NavLink to="/metrics" className={linkClass}>
                    Metrics
                </NavLink>

                <NavLink to="/alerts" className={linkClass}>
                    Alerts
                </NavLink>

            </nav>

        </div>
    );
}

export default Sidebar;
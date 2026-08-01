import ThemeToggle from "./ThemeToggle";

function Navbar() {
    return (
        <div className="bg-white dark:bg-slate-900 text-slate-900
        dark:text-white shadow px-6 py-4 flex justify-between transition-colors
        ">
            <h2 className="text-xl font-semibold">
                Dashboard
            </h2>
            <div className="flex items-center gap-4">
                <ThemeToggle />
                <button className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded">
                    Logout
                </button>
            </div>
        </div>
    );
}

export default Navbar;
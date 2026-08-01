import Sidebar from "../components/DashComponents/Sidebar";
import Navbar from "../components/DashComponents/Navbar";

function MainLayout({ children }) {
    return (
        <div className="flex">
            <Sidebar />
            <div className="
            flex-1
            min-h-screen
            bg-slate-100
            dark:bg-slate-950
            transition-colors
            ">
                <Navbar />
                <main className="p-6">
                    {children}
                </main>
            </div>
        </div>
    );
}

export default MainLayout;
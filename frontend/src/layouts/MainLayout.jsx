import Sidebar from "../components/DashComponents/Sidebar";
import Navbar from "../components/DashComponents/Navbar";

function MainLayout({ children }) {

    return (
        <div className="flex min-h-screen bg-slate-100 dark:bg-slate-950">

            <Sidebar />

            <div className="flex-1 flex flex-col">

                <Navbar />

                <main className="p-8">
                    {children}
                </main>

            </div>

        </div>
    );
}

export default MainLayout;
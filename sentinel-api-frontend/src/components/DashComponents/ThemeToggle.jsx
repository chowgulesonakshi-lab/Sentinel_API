import { useTheme } from "../../context/ThemeContext";
import { Sun, Moon } from "lucide-react";


function ThemeToggle(){

    const { darkMode, setDarkMode } = useTheme();


    return (
        <button
            onClick={() => setDarkMode(!darkMode)}
            className="px-3 py-2 rounded-lg
                bg-slate-200 dark:bg-slate-700
                text-slate-700 dark:text-slate-200
                hover:bg-slate-300 dark:hover:bg-slate-600
                transition-colors"
        >
            {darkMode ? (
                <Sun size={18} strokeWidth={2} className="text-yellow-400"/>
            ) : (
                <Moon size={18} strokeWidth={2} />
            )}
        </button>
    );
}


export default ThemeToggle;
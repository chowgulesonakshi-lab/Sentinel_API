import { useTheme } from "../../context/ThemeContext";


function ThemeToggle(){

    const { darkMode, setDarkMode } = useTheme();


    return (
        <button
            onClick={() => setDarkMode(!darkMode)}
            className="px-3 py-2 rounded-lg bg-slate-200 dark:bg-slate-700"
        >
            {darkMode ? "☀️" : "🌙"}
        </button>
    );
}


export default ThemeToggle;
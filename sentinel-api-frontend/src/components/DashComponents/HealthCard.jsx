import { useNavigate } from "react-router-dom";

function HealthCard({ healthy, down, unknown }) {
    const navigate = useNavigate();
    return (
        <div className="
            rounded-2xl p-6
            bg-white dark:bg-slate-900
            border border-slate-200 dark:border-slate-800
            shadow-sm
            hover:shadow-md
            transition-all
        ">


            <h2 className="
                text-lg font-semibold
                text-slate-800 dark:text-white
                mb-5
            ">
                API Health Overview
            </h2>



            <div className="grid grid-cols-3 gap-4">


                <div className="
                    p-4 rounded-xl
                    bg-green-500/10
                    hover:bg-green-500/20
                    dark:hover:bg-green-500/20
                    transition-all duration-300
                ">

                    <p className="
                        text-sm
                        text-slate-500 dark:text-slate-400
                    ">
                        Healthy
                    </p>

                    <h3 className="
                        text-3xl
                        font-bold
                        text-green-500
                    ">
                        {healthy}
                    </h3>

                </div>



                <div className="
                    p-4 rounded-xl
                    bg-red-500/10
                    hover:bg-red-500/20
                    dark:hover:bg-red-500/20
                    transition-all duration-300
                ">

                    <p className="
                        text-sm
                        text-slate-500 dark:text-slate-400
                    ">
                        Down
                    </p>

                    <h3 className="
                        text-3xl
                        font-bold
                        text-red-500
                    ">
                        {down}
                    </h3>

                </div>




                <div className="
                    p-4 rounded-xl
                    bg-yellow-500/10
                    hover:bg-yellow-500/20
                    dark:hover:bg-yellow-500/20
                    transition-all duration-300
                ">

                    <p className="
                        text-sm
                        text-slate-500 dark:text-slate-400
                    ">
                        Unknown
                    </p>

                    <h3 className="
                        text-3xl
                        font-bold
                        text-yellow-500
                    ">
                        {unknown}
                    </h3>

                </div>


            </div>



            <div className="flex justify-end mt-5">

                <button
                    onClick={() => navigate("/services")}
                    className="text-blue-600 dark:text-blue-400 hover:underline transition"
                >
                    View All Services →
                </button>

            </div>


        </div>
    );
}


export default HealthCard;
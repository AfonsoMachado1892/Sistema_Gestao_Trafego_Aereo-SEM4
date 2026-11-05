#!/bin/bash

REPORT_FILE="simulation_report.txt"

show_header() {
    clear
    echo "========================================"
    echo "        AISafe C Simulation UI"
    echo "========================================"
}

pause() {
    echo
    read -p "Press ENTER to continue..."
}

build_project() {
    echo "Building project..."
    echo
    make clean && make
    return $?
}

select_test_file() {
    mapfile -t files < <(ls tests/*.json 2>/dev/null | sort)

    if [ "${#files[@]}" -eq 0 ]; then
        echo "No JSON files found in tests/."
        return 1
    fi

    while true; do
        show_header
        echo "Available flight plans:"
        echo

        for i in "${!files[@]}"; do
            printf "%d) %s\n" "$((i + 1))" "${files[$i]}"
        done

        echo "0) Back"
        echo
        read -p "Choose a flight plan: " choice

        if [ "$choice" = "0" ]; then
            return 1
        fi

        if [[ "$choice" =~ ^[0-9]+$ ]] &&
           [ "$choice" -ge 1 ] &&
           [ "$choice" -le "${#files[@]}" ]; then
            SELECTED_FILE="${files[$((choice - 1))]}"
            return 0
        fi

        echo
        echo "Invalid option."
        pause
    done
}

select_weather_scenario() {
    while true; do
        show_header
        echo "Weather scenario:"
        echo
        echo "0) No weather"
        echo "1) Realistic"
        echo "2) Medium"
        echo "3) High"
        echo "4) Intense"
        echo
        read -p "Choose weather option: " choice

        case "$choice" in
            0)
                WEATHER_ENABLED=0
                WEATHER_LABEL="No weather"
                WEATHER_FILE=""
                return 0
                ;;
            1)
                WEATHER_ENABLED=1
                WEATHER_LABEL="Realistic"
                WEATHER_FILE="data/weather_aca121_realistic.csv"
                return 0
                ;;
            2)
                WEATHER_ENABLED=1
                WEATHER_LABEL="Medium"
                WEATHER_FILE="data/weather_aca121_medium.csv"
                return 0
                ;;
            3)
                WEATHER_ENABLED=1
                WEATHER_LABEL="High"
                WEATHER_FILE="data/weather_aca121_high.csv"
                return 0
                ;;
            4)
                WEATHER_ENABLED=1
                WEATHER_LABEL="Intense"
                WEATHER_FILE="data/weather_aca121_intense.csv"
                return 0
                ;;
            *)
                echo
                echo "Invalid option."
                pause
                ;;
        esac
    done
}

run_simulation() {
    local plan_file="$1"

    if [ "$WEATHER_ENABLED" -eq 1 ]; then
        ./simulation "$plan_file" --weather "$WEATHER_FILE"
    else
        ./simulation "$plan_file"
    fi
}

print_report_summary() {
    if [ ! -f "$REPORT_FILE" ]; then
        echo "No report generated."
        return
    fi

    echo "Report summary:"
    echo
    grep -E "Total flights|Total steps|Total violations|Global result" "$REPORT_FILE"
    echo
}

run_one_plan() {
    if select_test_file; then
        select_weather_scenario
        show_header

        if ! build_project; then
            echo
            echo "Build failed. Simulation was not executed."
            pause
            return
        fi

        show_header
        echo "Running simulation"
        echo
        echo "Plan    : $SELECTED_FILE"
        echo "Weather : $WEATHER_LABEL"
        echo "========================================"
        echo

        run_simulation "$SELECTED_FILE"

        echo
        echo "========================================"
        echo "Simulation finished"
        echo "========================================"
        echo

        print_report_summary
        pause
    fi
}

run_all_plans() {
    show_header

    mapfile -t files < <(ls tests/*.json 2>/dev/null | sort)

    if [ "${#files[@]}" -eq 0 ]; then
        echo "No JSON files found in tests/."
        pause
        return
    fi

    select_weather_scenario
    show_header

    if ! build_project; then
        echo
        echo "Build failed. Simulations were not executed."
        pause
        return
    fi

    show_header
    echo "Running all test plans"
    echo "Weather : $WEATHER_LABEL"
    echo "========================================"

    total="${#files[@]}"
    current=1

    for file in "${files[@]}"; do
        echo
        echo
        echo "========================================================="
        echo " RUNNING SIMULATION $current/$total"
        echo "========================================================="
        echo "Plan    : $file"
        echo "Weather : $WEATHER_LABEL"
        echo "========================================================="
        echo

        run_simulation "$file"

        echo
        echo "========================================"
        echo "SIMULATION REPORT $current/$total"
        echo "========================================"
        print_report_summary
        echo "========================================"
        echo
        echo

        current=$((current + 1))
    done

    pause
}

view_report() {
    while true; do
        show_header

        if [ ! -f "$REPORT_FILE" ]; then
            echo "No report found. Run a simulation first."
            pause
            return
        fi

        echo "1) View full report"
        echo "2) View report summary"
        echo "0) Back"
        echo
        read -p "Choose an option: " choice

        case "$choice" in
            1)
                clear
                cat "$REPORT_FILE"
                echo
                read -p "Press ENTER to go back..."
                ;;
            2)
                clear
                head -40 "$REPORT_FILE"
                echo
                read -p "Press ENTER to go back..."
                ;;
            0)
                return
                ;;
            *)
                echo
                echo "Invalid option."
                pause
                ;;
        esac
    done
}

main_menu() {
    while true; do
        show_header
        echo "1) Run a flight plan"
        echo "2) Run all test plans"
        echo "3) View simulation report"
        echo "0) Exit"
        echo "========================================"
        read -p "Choose an option: " option

        case "$option" in
            1) run_one_plan ;;
            2) run_all_plans ;;
            3) view_report ;;
            0)
                echo "Exiting..."
                exit 0
                ;;
            *)
                echo
                echo "Invalid option."
                pause
                ;;
        esac
    done
}

main_menu
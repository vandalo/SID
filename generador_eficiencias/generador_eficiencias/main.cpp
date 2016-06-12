//
//  main.cpp
//  generador_eficiencias
//
//  Created by Ferran coma rosell on 12/6/16.
//  Copyright © 2016 Ferran coma rosell. All rights reserved.
//

#include <iostream>
#include <fstream>
#include <random>
using namespace std;

int main(int argc, const char * argv[]) {
    ofstream fs("eficiencia.txt");
    int hores;
    cout << "Quantas horas quieres generar? "<< endl;
    cin >> hores;
    float randomNum;
    for(int i = 0; i < hores; ++i){
        random_device rd; // generates a random from harward
        mt19937 eng(rd()); // seed the generator
        uniform_int_distribution<> distr(1, 50); // define the range
        randomNum = distr(eng);
        fs << randomNum << endl;
    }
    cout << "Fitxer generat correctament." << endl;
}

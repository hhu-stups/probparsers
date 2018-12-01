#include <iostream>
#include <string>
#include "BUtils.cpp"
#include "BSet.cpp"
#include "BInteger.cpp"
#include "BBoolean.cpp"

#ifndef SetCardSmall2_H
#define SetCardSmall2_H

using namespace std;

class SetCardSmall2 {



    private:



        BInteger counter;
        BSet<BInteger > set;
        BInteger result;

        bool initialized = false;

    public:

        SetCardSmall2(){}

        void initialize() {
            if(initialized) {
                throw runtime_error("Machine is already initialized");
            }
            counter = static_cast<BInteger >((BInteger(0)));
            set = static_cast<BSet<BInteger > >((BSet<BInteger >((BInteger(1)))));
            result = static_cast<BInteger >((BInteger(0)));
            initialized = true;
        }

        void simulate() {
            if(!initialized) {
                throw runtime_error("Machine was not initialized");
            }
            while((counter.less((BInteger(5000000)))).booleanValue()) {
                result = static_cast<BInteger >(set.card());
                counter = static_cast<BInteger >(counter.plus((BInteger(1))));
            }
        }

};

int main() {
    clock_t start,finish;
    double time;
    SetCardSmall2 exec;
    exec.initialize();
    start = clock();
    exec.simulate();
    finish = clock();
    time = (double(finish)-double(start))/CLOCKS_PER_SEC;
    printf("%f\n", time);
    return 0;
}
#endif

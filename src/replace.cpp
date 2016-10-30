#include <streambuf>
#include <sstream>
#include <iostream>
#include <fstream>
#include <iomanip>
#include <string>
#include <vector>
#include <thread>
#include <atomic>

using namespace std;

class Map {
public:
    string key;
    string value;
    Map(string _key, string _value) {
        key = _key; value = _value;
    }
};

void replace(string, string);
void task(int, string, const vector<Map>&);
void replace(string&, const string&, const string&);
bool loadcsv(vector<Map> &, string);
void loadsrc(vector<string> &, string);
void showUsage();

void main(int argc, char *argv[]) {
    switch (argc) {
    case 2: replace(argv[1], "map.csv"); cout << ">> 工作完毕!" << endl; break;
    case 3: replace(argv[1], argv[2]); cout << ">> 工作完毕!" << endl; break;
    default: showUsage();
    }
}

void replace(string suffix, string csv) {
    vector<Map> csvmap;
    vector<string> srcs;

    if (loadcsv(csvmap, csv)) {
        loadsrc(srcs, suffix);
        int size = srcs.size();
        int width = 1;
        int temp = size;
        while (temp /= 10) width++;

        vector<thread> threads;
        for (int i = 0; i < size; i++) {
            cout << ">> 任务进度:[" << setw(width) << i << "/"
                << setw(width) << size << "] 当前队列:[" << srcs[i] << "]" << endl;
            threads.push_back(thread(task, i, srcs[i], csvmap));
        }
        cout << ">> 任务已全部加载,正在处理..." << endl;
        for (int i = 0; i < size; i++) {
            threads[i].join();
        }
    }
    else {
        cout << ">> 文件 [" << csv << "] 读取失败 , 请确认后重试!" << endl;
    }
}
void task(int id, string path, const vector<Map>& csvmap) {
    fstream srcfile(path);
    stringstream buff;
    buff << srcfile.rdbuf();
    string contents(buff.str());
    long size = csvmap.size();
    for (long i = 0; i < size; i++) {
        replace(contents, csvmap[i].key, csvmap[i].value);
    }
    srcfile.seekg(0);
    srcfile << contents;
    srcfile.close();
}
void replace(string& src, const string& before, const string& after) {
    int pos = 0;
    int bs = before.size();
    int as = after.size();
    while ((pos = src.find(before, pos)) != string::npos) {
        src.replace(pos, bs, after);
        pos += as;
    }
}
bool loadcsv(vector<Map> &csvmap, string csv) {
    cout << ">> 读取映射表..." << endl;
    ifstream file(csv, ios::in);
    if (file.is_open()) {
        string buff;
        while (getline(file, buff)) {
            int dot = buff.find_first_of(',');
            Map map(buff.substr(0, dot), buff.substr(dot + 1));
            csvmap.push_back(map);
        }
        file.close();
        cout << ">> 映射表 [" << csv << "] 读取完毕!" << endl;
        return true;
    }
    file.close();
    cout << ">> 映射表 [" << csv << "] 读取失败!" << endl;
    return false;
}
void loadsrc(vector<string> &srcs, string suffix) {
    cout << ">> 读取待转换文件..." << endl;
    system("dir /b/a-d/s > list");
    ifstream file("list", ios::in);
    string buff;
    while (file >> buff) {
        if (buff.substr(buff.find_last_of('.') + 1) == suffix) {
            srcs.push_back(buff);
        }
    }
    file.close();
    cout << ">> [" << srcs.size() << "]个文件读取完毕!" << endl;
}
void showUsage() {
    cout << "=========================================" << endl;
    cout << "=  用法:                                =" << endl;
    cout << "=     replace <suffix> [mapfile]        =" << endl;
    cout << "=========================================" << endl;
    cout << endl;
}
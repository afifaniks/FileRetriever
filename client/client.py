import socket
import time


host = "192.168.0.102"
port = 6969
buffer_size = 8000

def main():
    s = socket.socket()
    s.connect((host, port))

    file_name = input("Insert Filename: ")

    if file_name != "q":
        s.send(file_name.encode())

        data = s.recv(1024).decode()
        
        if data[:2] == "OK":
            file_size = int(data[2:])
            action = input("File exists. Total size: " + str(file_size) + " Bytes. Download? Y/N ->")
            
            if action == "Y" or action == "y":
                s.send("Y".encode())

                start_time = time.time()
                f = open(file_name, "wb")
                
                data = s.recv(buffer_size)
                total = len(data)
                f.write(data)

                while total < file_size:
                    data = s.recv(buffer_size)
                    total += len(data)
                    f.write(data)

                    print("Downloaded: " + str(round((total/file_size)*100, 2)) + "%")
                end_time = time.time()
                time_taken = end_time - start_time
                print("Download Completed! Time Taken: " + str(time_taken) + " seconds.")

        else:
            print("File does not exist.")
    s.close()

if __name__ == "__main__":
    main()

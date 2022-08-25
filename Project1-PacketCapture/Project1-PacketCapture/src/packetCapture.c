/*
 * packetCapture.c
 *
 *  Created on: Oct 14, 2013
 *      Author: Jacob Saunders
 *  This program reads in packets from either a pcap file or an ethernet
 *  adapter, and writes out what the packets contains to the terminal
 *  (And optionally a file). It will either end when it runs out of
 *  packets from a file or hits an optional count of packets.
 *  Parameters: -i interface name (such as eth0 or wlan0)
 *  -f pcap file (to read in from)
 *  -o filename (file to output to)
 *  -l number (limit number of packets read before ending the program)
 *
 *  Credit goes to the tutorial at http://www.tcpdump.org/pcap.htm for some of the
 *  code used here.
 */
#include "packetCapture.h"

//Define a boolean variable type and associated keywords entirely
//because I am used to languages with them. :)
#define bool int
#define true 1
#define false 0

/*
 * This method opens the source passed in inputSource and returns the pcap_t
 * file allowing retrieval of packets from it.
 * inputFile - is this reading from a file as opposed to a device?
 * inputSource - string of the device name or pcap file to read from.
 */
pcap_t* openSource(bool inputFile, const char* inputSource)
{
	//Branch based on whether this is a file input or not.
	pcap_t* source;
	char errbuf[PCAP_ERRBUF_SIZE];

	if (inputFile)
	{
		source = pcap_open_offline(inputSource, errbuf);
	}
	else
	{
		//Reading from a device. The true sets it into promiscuous mode.
		source = pcap_open_live(inputSource, BUFSIZ, true, 1000, errbuf);
	}

	if (source == NULL)
	{
		printf("Issue opening %s, error is: %s", inputSource, errbuf);
		exit(-1);
	}

	return source;
}

void installFilter(pcap_t* source, const char* filter)
{
	struct bpf_program filterProgram;

	//Compile and install the filter on the source.
	if (pcap_compile(source, &filterProgram, filter, 0, 0) == -1)
	{
		printf("Unable to compile filter, error: %s\n", pcap_geterr(source));
		printf("No filter installed.\n");
		return;
	}
	if (pcap_setfilter(source, &filterProgram) == -1)
	{
		printf("Unable to set filter, error: %s\n", pcap_geterr(source));
		printf("No filter installed.\n");
	}
}

/*
 * This method interpets the arguments given by the user and then passes
 * them to the appropriate methods.
 */
int main ( int argc, char *argv[] )
{
	printf("Packet Analyzer\n");

	//These variables track the style of input recieved.
	//Only has input must be true.
	bool hasInput = false;
	bool hasOutput = false;
	bool inputFile = false;
	char* inputSource = NULL;
	char* outputFilename = NULL;
	int packetLimit = -1;

	int i;
	for (i = 1; i < argc; i += 2)
	{
		//If argc is not at least two higher, there can't be a second
		//part to the flag.
		if ((i+2) > argc)
		{
			continue;
		}
		//Branch based on the flag passed.
		if (strcmp(argv[i], "-i") == 0)
		{
			if (hasInput)
			{
				printf("Cannot have two inputs.\n");
				exit(-1);
			}
			inputSource = argv[i+1];
			hasInput = true;
		}
		else if (strcmp(argv[i], "-f") == 0)
		{
			if (hasInput)
			{
				printf("Cannot have two inputs.\n");
				exit(-1);
			}
			inputSource = argv[i+1];
			hasInput = true;
			inputFile = true;
		}
		else if (strcmp(argv[i], "-o") == 0)
		{
			if (hasOutput)
			{
				printf("Cannot have two outputs.\n");
				exit(-1);
			}
			outputFilename = argv[i+1];
			hasOutput = true;
		}
		else if (strcmp(argv[i], "-l") == 0)
		{
			if (packetLimit > 0)
			{
				printf("Cannot set packet limit twice.\n");
				exit(-1);
			}
			enum StrToIntError error = strToInt(&packetLimit, argv[i+1], 10);
			if (error != SUCCESS)
			{
				printf("Error converting number %s\n", argv[i+1]);
				exit(-1);
			}
			else if (packetLimit <= 0)
			{
				printf("Invalid packet limit (must be positive) %i\n",
						packetLimit);
				exit(-1);
			}
		}
	}//End of argument for loop.

	//Open the output file (if one)
	FILE* outputFile = NULL;
	if (hasOutput)
	{
		outputFile = fopen(outputFilename, "w");
		if (outputFile == NULL)
		{
			printf("Unable to write to output file %s\n", outputFilename);
			exit(-1);
		}
	}

	//Test with output.
	if (!hasInput)
	{
		printf("Using default input device.\n");
		char errbuf[PCAP_ERRBUF_SIZE];
		inputSource = pcap_lookupdev(errbuf);
		if (inputSource == NULL)
		{
			printf("Unable to find default device, error %s\n", errbuf);
			exit(-1);
		}
	}
	if (inputFile)
	{
		printf("Reading from input file: %s\n", inputSource);
	}
	else
	{
		printf("Reading from interface: %s\n", inputSource);
	}
	if (hasOutput)
	{
		printf("Saving to file: %s\n", outputFilename);
	}
	if (packetLimit > 0)
	{
		printf("Packet limit: %i\n", packetLimit);
	}

	//Open the device.
	pcap_t* source = openSource(inputFile, inputSource);

	//Install a filter to remove all non TCP or UDP packets.
	installFilter(source, "tcp or udp");

	//Finally, read the packets.
	readPackets(source, outputFile, packetLimit);

	//Finish.
	pcap_close(source);
	if (outputFile != NULL)
	{
		fclose(outputFile);
	}
	printf("SUCCESS! Exiting...\n");
	exit(0);
}

/*
 * Reads in a number of packets from the pcap_t source until either it
 * is exausted of packets, or the packetLimit passed is reached.
 * source - pcap source to read packets from.
 * outputFile - file to output results to as well as the screen.
 * NULL if not desired to output to screen.
 * packetLimit - maximum number of packets to read.
 */
void readPackets(pcap_t* source, FILE* outputFile, int packetLimit)
{
	int linktype;

	// Determine the datalink layer type.
	if ((linktype = pcap_datalink(source)) < 0)
	{
		printf("Error getting datalink size.: %s\n", pcap_geterr(source));
		return;
	}

	// Set the datalink layer header size.
	switch (linktype)
	{
	case DLT_NULL:
		linkHeaderSize = 4;
		break;

	case DLT_EN10MB:
		linkHeaderSize = 14;
		break;

	case DLT_SLIP:
	case DLT_PPP:
		linkHeaderSize = 24;
		break;

	default:
		printf("Unsupported datalink (%d)\n", linktype);
		return;
	}

	//Loop until we run out of packets, hit the limit (if one)
	//(or there's an error.)
	bool isLimit = packetLimit > 0;
	int currentPacket = 0;
	while ((!isLimit) || (currentPacket < packetLimit))
	{
		//Pointers for the packet.
		struct pcap_pkthdr *packetHeader;
		const u_char *packetData;

		//Read packet, check if error.
		int result = pcap_next_ex(source, &packetHeader, &packetData);
		if (result == 0)
		{
			//Timeout, loop again until we get another packet.
			continue;
		}
		else if (result == -1)
		{
			printf("Error reading packet: %s\n", pcap_geterr(source));
			exit(-1);
		}
		else if (result == -2)
		{
			//Out of packets in file.
			break;
		}

		//Handle the packet.
		handlePacket(currentPacket, packetHeader, packetData, outputFile);

		//Every hundred packets, flush the contents of the file to the disk.
		if (outputFile != NULL && currentPacket % 100 == 0)
		{
			fflush(outputFile);
		}

		//Increment packet count.
		currentPacket++;
	}
}

/*
 * Outputs a string to both stdin and the passed file.
 * string - pointer to a string to print. Must be valid.
 * file - file to write to. Must be valid.
 */
void outputString(const char* string, FILE* file)
{
	printf(string);
	if (file != NULL)
	{
		fprintf(file, string);
	}
}

/*
 * This takes the packet passed by another method and if it is a TCP packet,
 * analyzes it and prints the results to the screen and an output file (if one).
 * packetHeader - packet header returned from pcap_next_ex function.
 * packetData - packet data returned from pcap_next_ex function.
 * outputFile - file to output results from (NULL if not desired)
 * linkHeaderSize - how large the header from the data link layer on this source
 * is.
 */
void handlePacket(int packetNum, struct pcap_pkthdr *packetHeader, const u_char *packetData,
		FILE* outputFile)
{
	//Designed with help from tutorial at:
	//http://vichargrave.com/develop-a-packet-sniffer-with-libpcap/

	struct ip* ipHeader;
	struct tcphdr* tcpHeader;
	struct udphdr* udpHeader;
	char sourceIP[256], destIP[256];
	char* message;

	//Get information from the packet.
	double timestamp = ((double)packetHeader->ts.tv_usec);
	int usec = pow(10,6);
	timestamp /= usec;
	timestamp += packetHeader->ts.tv_sec;

	//Print the messages. You need to do a free after an asprintf.
	asprintf(&message, "Packet Number: %d Timestamp: %f\n", packetNum, timestamp);
	outputString(message, outputFile);
	free(message);

	//Get the info from the IP header of the packet and print it.
	u_char *packetStart = ((u_char*)packetData) + linkHeaderSize;
	ipHeader = (struct ip*) packetStart;
	strcpy(sourceIP, inet_ntoa(ipHeader->ip_src));
	strcpy(destIP, inet_ntoa(ipHeader->ip_dst));

	//Output IP header info.
	asprintf(&message,
			"ID:%d Service Type:%d, Time To Live:%d IP Header Length:%d Total Length:%d\n",
			ntohs(ipHeader->ip_id), ipHeader->ip_tos, ipHeader->ip_ttl,
			4*ipHeader->ip_hl, ntohs(ipHeader->ip_len));
	outputString(message, outputFile);
	free(message);

	//Advance to next header and parse.
	packetStart += 4*ipHeader->ip_hl;
	switch (ipHeader->ip_p)
	{
	//Handle TCP packets.
	case IPPROTO_TCP:
		tcpHeader = (struct tcphdr*) packetStart;
		//HANDLE TCP PACKETS HERE.
		outputString("TCP Packet.\n", outputFile);
		break;

	//Handle UDP packets.
	case IPPROTO_UDP:
		udpHeader = (struct udphdr*)packetStart;
		//HANDLE UDP PACKETS HERE.
		outputString("TCP Packet.\n", outputFile);
		break;
	}
	outputString("--------------------------\n", outputFile);
}



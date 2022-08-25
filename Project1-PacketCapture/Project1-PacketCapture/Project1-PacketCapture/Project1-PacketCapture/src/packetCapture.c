
#include "packetCapture.h"
#include <string.h>


struct storage_syn
{

  char ip_source[256];
  long int count;
};


typedef struct storage_syn node;

node *head;

node* calllinkedlist();

long int k=0;
long int j=0;

char *a;
char *b;
char *c;
char *d;

long int countconnection=0;
node *syn[1000000];



#define bool int
#define true 1
#define false 0

long int countsyn=0;
long int countsynack=0;
long int countackrst=0;
char sourceipaddress[256];
char destinationipaddress[256];
char cd[256], ef[256];
void print_hex_ascii_line(const u_char *payload,int len,int offset)
{
	int i;
	int gap;
	const u_char *ch;

	//offset

	printf("offset is :%05d",offset);

	ch=payload;

	for (i=0;i<len;i++)
	{
		printf("%02x",*ch);
		ch++;
		if(i==7)
			printf(" ");
	}
	if(len<8)
	{ printf(" ");
	}
	if(len<16)
	{
		gap=16-len;
		for(i=0;i<gap;i++)
		{
			printf(" ");
		}
	}
	printf(" ");
	ch=payload;
	for(i=0;i<len;i++)
	{
		if(isprint(*ch))
			printf("%c",*ch);
		else
			printf(".");
	}
	printf("\n");
	return;
}
bool syncompareipaddress(char sourceip[256])
{

	char test[256];
	int cmpres;
	long int count;
	for(k=0;k<countsyn;k++)
	{
		strcpy(test,syn[k]->ip_source);
		cmpres=strcmp(test,sourceip);
		if(cmpres==0){

			count=syn[k]->count;
			count++;
			syn[k]->count=count;

			return true;
		}

	}
	return false;
}

void createlinkedlist(char a[254])
{
bool match;


	match=syncompareipaddress(a);

	if(match==false){


			syn[countsyn]=calllinkedlist();




			strcpy(syn[countsyn]->ip_source,a);

			syn[countsyn]->count=1;

			countsyn++;






	}



}


void synackcompareipaddress(char destip[256])
{
int cmpres;
long int count;
char test[256];
for(k=0;k<countsyn;k++)
{

	strcpy(test,syn[k]->ip_source);
	cmpres=strcmp(test,destip);
	if(cmpres==0)
	{

		syn[k]->count--;

	}
}



}

void printfailureconnection()
{

	for(k=0;k<countsyn;k++)
	{

			if(syn[k]->count>0)
			{
				printf("The failure connection for the ipaddress %s is %d \n",syn[k]->ip_source,syn[k]->count);
			}

	}
}

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
	printfailureconnection();
	printf("SUCCESS! Exiting...\n");
	exit(0);


}

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


	struct ip* ipHeader;
	struct tcphdr* tcpHeader;
	struct udphdr* udpHeader;
	char sourceIP[256], destIP[256];
	char* message;

	u_char *payload;

	double timestamp = ((double)packetHeader->ts.tv_usec);
	int usec = pow(10,6);
	timestamp /= usec;
	timestamp += packetHeader->ts.tv_sec;
	u_char *packetStart = ((u_char*)packetData) + linkHeaderSize;
	ipHeader = (struct ip*) packetStart;
	strcpy(sourceIP, inet_ntoa(ipHeader->ip_src));
	strcpy(destIP, inet_ntoa(ipHeader->ip_dst));

	packetStart += 4*ipHeader->ip_hl;
	switch (ipHeader->ip_p)
	{
	//Handle TCP packets.
	case IPPROTO_TCP:
		tcpHeader = (struct tcphdr*) packetStart;
		//HANDLE TCP PACKETS HERE.
		//Printing of Source and Destination Ip address, Sequence Number



						if( tcpHeader->syn== 1 && tcpHeader->ack==0)
						{

							createlinkedlist(sourceIP);

						}



						if((( tcpHeader->syn)== 1) && ((tcpHeader->ack)==1))
						{



							synackcompareipaddress(destIP);


						}


//		outputString("TCP Packet.\n", outputFile);
		break;

	//Handle UDP packets.
	case IPPROTO_UDP:
		udpHeader = (struct udphdr*)packetStart;
		//HANDLE UDP PACKETS HERE.
		//outputString("TCP Packet.\n", outputFile);
		break;
	}

	/*
	 *
	 printf("the total number of synchronous packets are as follows");
	  struct storage_syn *display_ip_address;
	  display_ip_address=start;
	  while(display_ip_address!=NULL)
	  {
		  asprintf(&message, " The source ipaddress is : %s,Destination ipaddress %s", inet_ntoa(display_ip_address->ip_source), inet_ntoa(display_ip_address->ip_destination));
		  outputString(message,outputFile);
		   free(message);
		   display_ip_address=display_ip_address->next;
	  }

*/

}

void print_payload(const u_char *payload, int len)
{
	int len_rem = len;
		int line_width = 16;			/* number of bytes per line */
		int line_len;
		int offset = 0;

	const u_char *ch=payload;

	if(len<=0)
	return;
	if(len<=line_width)
	{
		print_hex_ascii_line(ch,len,offset);
		return;
	}
	/* data spans multiple lines */
	for ( ;; ) {
		/* compute current line length */
		line_len = line_width % len_rem;
		/* print line */
		print_hex_ascii_line(ch, line_len, offset);
		/* compute total remaining */
		len_rem = len_rem - line_len;
		/* shift pointer to remaining bytes to print */
		ch = ch + line_len;
		/* add offset */
		offset = offset + line_width;
		/* check if we have line width chars or less */
		if (len_rem <= line_width) {
			/* print last line and get out */
			print_hex_ascii_line(ch, len_rem, offset);
			break;
		}
	}

return;

}

node* calllinkedlist()
{
	node *p,*head;
	int k=0;
	if(k==0)
	{

		head=(node *)malloc(sizeof(node));
		p=head;
	}
	return p;
}




package pokerBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;

import pokerBase.Card;
import pokerBase.Hand;
import pokerEnums.eCardNo;
import pokerEnums.eHandStrength;
import pokerEnums.eRank;
import pokerEnums.eSuit;
import pokerEnums.eGame;

public class Hand {
	private UUID playerID;
	@XmlElement
	private ArrayList<Card> CardsInHand;
	private ArrayList<Card> BestCardsInHand;

	@XmlElement
	private int HandStrength;
	@XmlElement
	private int HiHand;
	@XmlElement
	private int LoHand;
	@XmlElement
	private ArrayList<Card> Kickers = new ArrayList<Card>();

	private boolean bScored = false;

	private boolean Flush;
	private boolean Straight;
	private boolean Ace;
	private static Deck dJoker = new Deck();

	public Hand() {

	}

	public void AddCardToHand(Card c) {
		if (this.CardsInHand == null) {
			CardsInHand = new ArrayList<Card>();
		}
		this.CardsInHand.add(c);
	}

	public Card GetCardFromHand(int location) {
		return CardsInHand.get(location);
	}

	public Hand(Deck d) {
		ArrayList<Card> Import = new ArrayList<Card>();
		for (int x = 0; x < 5; x++) {
			Import.add(d.drawFromDeck());
		}
		CardsInHand = Import;
	}

	public Hand(ArrayList<Card> setCards) {
		this.CardsInHand = setCards;
	}

	public ArrayList<Card> getCards() {
		return CardsInHand;
	}

	public ArrayList<Card> getBestHand() {
		return BestCardsInHand;
	}

	public void setPlayerID(UUID playerID) {
		this.playerID = playerID;
	}

	public UUID getPlayerID() {
		return playerID;
	}

	public void setBestHand(ArrayList<Card> BestHand) {
		this.BestCardsInHand = BestHand;
	}

	public int getHandStrength() {
		return HandStrength;
	}

	public ArrayList<Card> getKicker() {
		return Kickers;
	}

	public int getHighPairStrength() {
		return HiHand;
	}

	public int getLowPairStrength() {
		return LoHand;
	}

	public boolean getAce() {
		return Ace;
	}

	public static Hand EvalHand(ArrayList<Card> SeededHand) {

		Deck d = new Deck();
		Hand h = new Hand(d);
		h.CardsInHand = SeededHand;

		return h;
	}
	public static Hand PickBestHand(ArrayList<Hand> Hands) throws exHand
	{
		Collections.sort(Hands, HandRank);
		if (Hands.get(0).getHandStrength() == Hands.get(1).getHandStrength())
		{
			throw  new exHand();
		}
		return Hands.get(0);
	}

	ArrayList<Hand> jokerHands = new ArrayList<Hand>();

	public void EvalHand() {
		// Evaluates if the hand is a flush and/or straight then figures out
		// the hand's strength attributes

		ArrayList<Card> remainingCards = new ArrayList<Card>();
		int numberOfJokers = 0;

		for (int i = 0; i < 5; i++) {
			if (CardsInHand.get(i).getRank() == eRank.JOKER)

			{
				numberOfJokers++;
			}
		}
		// Sort the cards!
		Collections.sort(CardsInHand, Card.CardRank);

		if (numberOfJokers != 0 && numberOfJokers != 5) {
			for (int i = 0; i <= 3; i++) {
				eSuit SuitValue = eSuit.values()[i];
				for (int j = 0; j <= 12; j++) {
					eRank RankValue = eRank.values()[j];
					Card NewCard = new Card(SuitValue, RankValue, (13 * i) + j + 1);
					CardsInHand.set(eCardNo.FirstCard.getCardNo(), NewCard);
					Hand HandAdd = new Hand(CardsInHand);
					jokerHands.add(HandAdd);
				}
			}
			for (int i = 0; i < jokerHands.size(); i++) {
				jokerHands.get(i).EvalHand();
			}
			Collections.sort(jokerHands, HandRank);
		}
		if (numberOfJokers == 5) {
			ScoreHand(eHandStrength.RoyalFlush, 0, 0, null);
		}

		int numberOfWilds = 0;

		for (int i = 0; i < 5; i++) {
			if (CardsInHand.get(i).getRank() == eRank.JOKER)

			{
				numberOfWilds++;
			}
		}
		ArrayList<Hand> wildHands = new ArrayList<Hand>();
		Collections.sort(CardsInHand, Card.CardRank);
		if (numberOfWilds != 0 && numberOfWilds != 5) {
			for (short i = 0; i <= 3; i++) {
				eSuit SuitValue = eSuit.values()[i];
				for (short j = 0; j <= 12; j++) {
					eRank RankValue = eRank.values()[j];
					Card NewCard = new Card(SuitValue, RankValue, (13 * i) + j + 1);
					CardsInHand.set(eCardNo.FirstCard.getCardNo(), NewCard);
					Hand HandAdd = new Hand(CardsInHand);
					wildHands.add(HandAdd);
				}
			}

			for (short i = 0; i < wildHands.size(); i++) {
				wildHands.get(i).EvalHand();
			}
			Collections.sort(wildHands, HandRank);
		}
		if (numberOfWilds == 5) {
			ScoreHand(eHandStrength.RoyalFlush, 0, 0, null);
		}
		// Ace Evaluation
		if (CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank() == eRank.ACE) {
			Ace = true;
		}

		// Flush Evaluation
		if (CardsInHand.get(eCardNo.FirstCard.getCardNo()).getSuit() == CardsInHand.get(eCardNo.SecondCard.getCardNo())
				.getSuit()
				&& CardsInHand.get(eCardNo.FirstCard.getCardNo()).getSuit() == CardsInHand
						.get(eCardNo.ThirdCard.getCardNo()).getSuit()
				&& CardsInHand.get(eCardNo.FirstCard.getCardNo()).getSuit() == CardsInHand
						.get(eCardNo.FourthCard.getCardNo()).getSuit()
				&& CardsInHand.get(eCardNo.FirstCard.getCardNo()).getSuit() == CardsInHand
						.get(eCardNo.FifthCard.getCardNo()).getSuit()) {
			Flush = true;
		} else {
			Flush = false;
		}

		// Straight Evaluation
		if (Ace) {
			// Looks for Ace, King, Queen, Jack, 10
			if (CardsInHand.get(eCardNo.SecondCard.getCardNo()).getRank() == eRank.KING
					&& CardsInHand.get(eCardNo.ThirdCard.getCardNo()).getRank() == eRank.QUEEN
					&& CardsInHand.get(eCardNo.FourthCard.getCardNo()).getRank() == eRank.JACK
					&& CardsInHand.get(eCardNo.FifthCard.getCardNo()).getRank() == eRank.TEN) {
				Straight = true;
				// Looks for Ace, 2, 3, 4, 5
			} else if (CardsInHand.get(eCardNo.FifthCard.getCardNo()).getRank() == eRank.TWO
					&& CardsInHand.get(eCardNo.FourthCard.getCardNo()).getRank() == eRank.THREE
					&& CardsInHand.get(eCardNo.ThirdCard.getCardNo()).getRank() == eRank.FOUR
					&& CardsInHand.get(eCardNo.SecondCard.getCardNo()).getRank() == eRank.FIVE) {
				Straight = true;
			} else {
				Straight = false;
			}
			// Looks for straight without Ace
		} else if (CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank()
				.getRank() == CardsInHand.get(eCardNo.SecondCard.getCardNo()).getRank().getRank() + 1
				&& CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank()
						.getRank() == CardsInHand.get(eCardNo.ThirdCard.getCardNo()).getRank().getRank() + 2
				&& CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank()
						.getRank() == CardsInHand.get(eCardNo.FourthCard.getCardNo()).getRank().getRank() + 3
				&& CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank()
						.getRank() == CardsInHand.get(eCardNo.FifthCard.getCardNo()).getRank().getRank() + 4) {
			Straight = true;
		} else {
			Straight = false;
		}

		// Evaluates the hand type
		if (Straight == true && Flush == true && CardsInHand.get(eCardNo.FifthCard.getCardNo()).getRank() == eRank.TEN
				&& Ace) {
			ScoreHand(eHandStrength.RoyalFlush, 0, 0, null);
		}

		// Straight Flush
		else if (Straight == true && Flush == true) {
			remainingCards = null;
			ScoreHand(eHandStrength.StraightFlush, CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank().getRank(),
					0, remainingCards);
		}

		// five of a Kind

		else if (CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank() == CardsInHand
				.get(eCardNo.FifthCard.getCardNo()).getRank()) {
			remainingCards = null;
			ScoreHand(eHandStrength.FiveOfAKind, CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank().getRank(), 0,
					remainingCards);
		}

		// Four of a Kind

		else if (CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank() == CardsInHand
				.get(eCardNo.SecondCard.getCardNo()).getRank()
				&& CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank() == CardsInHand
						.get(eCardNo.ThirdCard.getCardNo()).getRank()
				&& CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank() == CardsInHand
						.get(eCardNo.FourthCard.getCardNo()).getRank()) {

			remainingCards.add(CardsInHand.get(eCardNo.FifthCard.getCardNo()));
			ScoreHand(eHandStrength.FourOfAKind, CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank().getRank(), 0,
					remainingCards);
		}

		else if (CardsInHand.get(eCardNo.FifthCard.getCardNo()).getRank() == CardsInHand
				.get(eCardNo.SecondCard.getCardNo()).getRank()
				&& CardsInHand.get(eCardNo.FifthCard.getCardNo()).getRank() == CardsInHand
						.get(eCardNo.ThirdCard.getCardNo()).getRank()
				&& CardsInHand.get(eCardNo.FifthCard.getCardNo()).getRank() == CardsInHand
						.get(eCardNo.FourthCard.getCardNo()).getRank()) {

			remainingCards.add(CardsInHand.get(eCardNo.FirstCard.getCardNo()));
			ScoreHand(eHandStrength.FourOfAKind, CardsInHand.get(eCardNo.FifthCard.getCardNo()).getRank().getRank(), 0,
					remainingCards);
		}

		// Full House
		else if (CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank() == CardsInHand
				.get(eCardNo.ThirdCard.getCardNo()).getRank()
				&& CardsInHand.get(eCardNo.FourthCard.getCardNo()).getRank() == CardsInHand
						.get(eCardNo.FifthCard.getCardNo()).getRank()) {
			remainingCards = null;
			ScoreHand(eHandStrength.FullHouse, CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank().getRank(),
					CardsInHand.get(eCardNo.FourthCard.getCardNo()).getRank().getRank(), remainingCards);
		}

		else if (CardsInHand.get(eCardNo.ThirdCard.getCardNo()).getRank() == CardsInHand
				.get(eCardNo.FifthCard.getCardNo()).getRank()
				&& CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank() == CardsInHand
						.get(eCardNo.SecondCard.getCardNo()).getRank()) {
			remainingCards = null;
			ScoreHand(eHandStrength.FullHouse, CardsInHand.get(eCardNo.ThirdCard.getCardNo()).getRank().getRank(),
					CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank().getRank(), remainingCards);
		}

		// Flush
		else if (Flush) {
			remainingCards = null;
			ScoreHand(eHandStrength.Flush, CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank().getRank(), 0,
					remainingCards);
		}

		// Straight
		else if (Straight) {
			remainingCards = null;
			ScoreHand(eHandStrength.Straight, CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank().getRank(), 0,
					remainingCards);
		}

		// Three of a Kind
		else if (CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank() == CardsInHand
				.get(eCardNo.ThirdCard.getCardNo()).getRank()) {

			remainingCards.add(CardsInHand.get(eCardNo.FourthCard.getCardNo()));
			remainingCards.add(CardsInHand.get(eCardNo.FifthCard.getCardNo()));
			ScoreHand(eHandStrength.ThreeOfAKind, CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank().getRank(), 0,
					remainingCards);
		}

		else if (CardsInHand.get(eCardNo.SecondCard.getCardNo()).getRank() == CardsInHand
				.get(eCardNo.FourthCard.getCardNo()).getRank()) {
			remainingCards.add(CardsInHand.get(eCardNo.FirstCard.getCardNo()));
			remainingCards.add(CardsInHand.get(eCardNo.FifthCard.getCardNo()));

			ScoreHand(eHandStrength.ThreeOfAKind, CardsInHand.get(eCardNo.SecondCard.getCardNo()).getRank().getRank(),
					0, remainingCards);
		} else if (CardsInHand.get(eCardNo.ThirdCard.getCardNo()).getRank() == CardsInHand
				.get(eCardNo.FifthCard.getCardNo()).getRank()) {
			remainingCards.add(CardsInHand.get(eCardNo.FirstCard.getCardNo()));
			remainingCards.add(CardsInHand.get(eCardNo.SecondCard.getCardNo()));
			ScoreHand(eHandStrength.ThreeOfAKind, CardsInHand.get(eCardNo.ThirdCard.getCardNo()).getRank().getRank(), 0,
					remainingCards);
		}

		// Two Pair
		else if (CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank() == CardsInHand
				.get(eCardNo.SecondCard.getCardNo()).getRank()
				&& (CardsInHand.get(eCardNo.ThirdCard.getCardNo()).getRank() == CardsInHand
						.get(eCardNo.FourthCard.getCardNo()).getRank())) {

			remainingCards.add(CardsInHand.get(eCardNo.FifthCard.getCardNo()));

			ScoreHand(eHandStrength.TwoPair, CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank().getRank(),
					CardsInHand.get(eCardNo.ThirdCard.getCardNo()).getRank().getRank(), remainingCards);
		} else if (CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank() == CardsInHand
				.get(eCardNo.SecondCard.getCardNo()).getRank()
				&& (CardsInHand.get(eCardNo.FourthCard.getCardNo()).getRank() == CardsInHand
						.get(eCardNo.FifthCard.getCardNo()).getRank())) {

			remainingCards.add(CardsInHand.get(eCardNo.ThirdCard.getCardNo()));

			ScoreHand(eHandStrength.TwoPair, CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank().getRank(),
					CardsInHand.get(eCardNo.FourthCard.getCardNo()).getRank().getRank(), remainingCards);
		} else if (CardsInHand.get(eCardNo.SecondCard.getCardNo()).getRank() == CardsInHand
				.get(eCardNo.ThirdCard.getCardNo()).getRank()
				&& (CardsInHand.get(eCardNo.FourthCard.getCardNo()).getRank() == CardsInHand
						.get(eCardNo.FifthCard.getCardNo()).getRank())) {

			remainingCards.add(CardsInHand.get(eCardNo.FirstCard.getCardNo()));
			ScoreHand(eHandStrength.TwoPair, CardsInHand.get(eCardNo.SecondCard.getCardNo()).getRank().getRank(),
					CardsInHand.get(eCardNo.FourthCard.getCardNo()).getRank().getRank(), remainingCards);
		}

		// Pair
		else if (CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank() == CardsInHand
				.get(eCardNo.SecondCard.getCardNo()).getRank()) {

			remainingCards.add(CardsInHand.get(eCardNo.ThirdCard.getCardNo()));
			remainingCards.add(CardsInHand.get(eCardNo.FourthCard.getCardNo()));
			remainingCards.add(CardsInHand.get(eCardNo.FifthCard.getCardNo()));
			ScoreHand(eHandStrength.Pair, CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank().getRank(), 0,
					remainingCards);
		} else if (CardsInHand.get(eCardNo.SecondCard.getCardNo()).getRank() == CardsInHand
				.get(eCardNo.ThirdCard.getCardNo()).getRank()) {
			remainingCards.add(CardsInHand.get(eCardNo.FirstCard.getCardNo()));
			remainingCards.add(CardsInHand.get(eCardNo.FourthCard.getCardNo()));
			remainingCards.add(CardsInHand.get(eCardNo.FifthCard.getCardNo()));
			ScoreHand(eHandStrength.Pair, CardsInHand.get(eCardNo.SecondCard.getCardNo()).getRank().getRank(), 0,
					remainingCards);
		} else if (CardsInHand.get(eCardNo.ThirdCard.getCardNo()).getRank() == CardsInHand
				.get(eCardNo.FourthCard.getCardNo()).getRank()) {

			remainingCards.add(CardsInHand.get(eCardNo.FirstCard.getCardNo()));
			remainingCards.add(CardsInHand.get(eCardNo.SecondCard.getCardNo()));
			remainingCards.add(CardsInHand.get(eCardNo.FifthCard.getCardNo()));

			ScoreHand(eHandStrength.Pair, CardsInHand.get(eCardNo.ThirdCard.getCardNo()).getRank().getRank(), 0,
					remainingCards);
		} else if (CardsInHand.get(eCardNo.FourthCard.getCardNo()).getRank() == CardsInHand
				.get(eCardNo.FifthCard.getCardNo()).getRank()) {

			remainingCards.add(CardsInHand.get(eCardNo.FirstCard.getCardNo()));
			remainingCards.add(CardsInHand.get(eCardNo.SecondCard.getCardNo()));
			remainingCards.add(CardsInHand.get(eCardNo.ThirdCard.getCardNo()));

			ScoreHand(eHandStrength.Pair, CardsInHand.get(eCardNo.FourthCard.getCardNo()).getRank().getRank(), 0,
					remainingCards);
		}

		else {
			remainingCards.add(CardsInHand.get(eCardNo.SecondCard.getCardNo()));
			remainingCards.add(CardsInHand.get(eCardNo.ThirdCard.getCardNo()));
			remainingCards.add(CardsInHand.get(eCardNo.FourthCard.getCardNo()));
			remainingCards.add(CardsInHand.get(eCardNo.FifthCard.getCardNo()));

			ScoreHand(eHandStrength.HighCard, CardsInHand.get(eCardNo.FirstCard.getCardNo()).getRank().getRank(), 0,
					remainingCards);
		}
	}

	private void ScoreHand(eHandStrength hST, int HiHand, int LoHand, ArrayList<Card> kickers) {
		this.HandStrength = hST.getHandStrength();
		this.HiHand = HiHand;
		this.LoHand = LoHand;
		this.Kickers = kickers;
		this.bScored = true;

	}

	/**
	 * Custom sort to figure the best hand in an array of hands
	 */
	public static Comparator<Hand> HandRank = new Comparator<Hand>() {

		public int compare(Hand h1, Hand h2) {

			int result = 0;

			result = h2.getHandStrength() - h1.getHandStrength();

			if (result != 0) {
				return result;
			}

			result = h2.getHighPairStrength() - h1.getHighPairStrength();
			if (result != 0) {
				return result;
			}

			result = h2.getLowPairStrength() - h1.getLowPairStrength();
			if (result != 0) {
				return result;
			}

			
			if ((h2.Kickers == null) || (h1.Kickers == null))
			{
				return 0;
			}
			

			try
			{
				if (h2.Kickers.size() >= eCardNo.FirstCard.getCardNo() +1 )
				{
					if (h1.Kickers.size() >= eCardNo.FirstCard.getCardNo() +1)
					{
						result = h2.getKicker().get(eCardNo.FirstCard.getCardNo()).getRank().getRank() - h1.getKicker().get(eCardNo.FirstCard.getCardNo()).getRank().getRank();
					}
					if (result != 0)
					{
						return result;
					}
				}				
			}
			catch (Exception e)
			{				
				System.out.println(e.getMessage());
				throw new RuntimeException(e);
			}			

			
			try
			{
				if (h2.Kickers.size() >= eCardNo.SecondCard.getCardNo() +1 )
				{
					if (h1.Kickers.size() >= eCardNo.SecondCard.getCardNo() +1)
					{
						result = h2.getKicker().get(eCardNo.SecondCard.getCardNo()).getRank().getRank() - h1.getKicker().get(eCardNo.SecondCard.getCardNo()).getRank().getRank();
					}
					if (result != 0)
					{
						return result;
					}
				}				
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
				throw new RuntimeException(e);				
			}			
			
			try
			{
				if (h2.Kickers.size() >= eCardNo.ThirdCard.getCardNo() +1 )
				{
					if (h1.Kickers.size() >= eCardNo.ThirdCard.getCardNo() +1)
					{
						result = h2.getKicker().get(eCardNo.ThirdCard.getCardNo()).getRank().getRank() - h1.getKicker().get(eCardNo.ThirdCard.getCardNo()).getRank().getRank();
					}
					if (result != 0)
					{
						return result;
					}
				}				
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
				throw new RuntimeException(e);
			}
			
			try
			{
				if (h2.Kickers.size() >= eCardNo.FourthCard.getCardNo() +1 )
				{
					if (h1.Kickers.size() >= eCardNo.FourthCard.getCardNo() +1)
					{
						result = h2.getKicker().get(eCardNo.FourthCard.getCardNo()).getRank().getRank() - h1.getKicker().get(eCardNo.FourthCard.getCardNo()).getRank().getRank();
					}
					if (result != 0)
					{
						return result;
					}
				}				
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
				throw new RuntimeException(e);				
			}
			
			return 0;
		}
	};

}
